package com.social.auth.service;

import com.social.auth.dto.request.ChefLoginRequest;
import com.social.auth.dto.request.LoginRequest;
import com.social.auth.dto.request.RegisterAgentRequest;
import com.social.auth.dto.request.RegisterChefRequest;
import com.social.auth.dto.request.TwoFaVerifyRequest;
import com.social.auth.dto.response.AuthResponse;
import com.social.auth.dto.response.ChefLoginResponse;
import com.social.auth.dto.response.TokenResponse;
import com.social.auth.dto.response.TwoFaSetupResponse;
import com.social.auth.entity.User;
import com.social.auth.enums.UserRole;
import com.social.auth.exception.AuthException;
import com.social.auth.repository.UserRepository;
import com.social.auth.security.TotpManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TotpManager totpManager;
    private final EmailService emailService;

    private final Map<String, String> tempSecrets = new HashMap<>();

    private static final Pattern CNI_PATTERN = Pattern.compile("^[A-Z0-9]{6,15}$");

    @Transactional
    public AuthResponse registerAgent(RegisterAgentRequest request) {
        log.info("Inscription d'un agent: {}", maskEmail(request.getEmail()));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw AuthException.emailAlreadyExists(request.getEmail());
        }

        if (request.getNumeroCni() != null && !CNI_PATTERN.matcher(request.getNumeroCni()).matches()) {
            throw AuthException.invalidCni();
        }

        try {
            User user = User.builder()
                    .email(request.getEmail().toLowerCase().trim())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName().trim())
                    .numeroCni(request.getNumeroCni())
                    .role(UserRole.AGENT)
                    .twoFactorEnabled(request.isTwoFactorEnabled())
                    .twoFactorVerified(false)
                    .accountNonLocked(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .enabled(true)
                    .failedAttempts(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            user = userRepository.save(user);
            log.info("Agent inscrit avec succès: ID={}", user.getId());

            String accessToken  = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return AuthResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .requiresTwoFactor(user.isTwoFactorEnabled())
                    .message("Inscription réussie")
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de l'inscription de l'agent: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'inscription", e);
        }
    }

    @Transactional
    public AuthResponse registerChef(RegisterChefRequest request) {
        log.info("Inscription d'un chef de ménage: {}", maskEmail(request.getEmail()));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw AuthException.emailAlreadyExists(request.getEmail());
        }

        if (!CNI_PATTERN.matcher(request.getNumeroCni()).matches()) {
            throw AuthException.invalidCni();
        }

        try {
            UUID menageUuid = null;
            if (request.getMenageId() != null && !request.getMenageId().isEmpty()) {
                try {
                    menageUuid = UUID.fromString(request.getMenageId());
                } catch (IllegalArgumentException e) {
                    log.warn("Format UUID invalide pour menageId: {}", request.getMenageId());
                }
            }

            User user = User.builder()
                    .email(request.getEmail().toLowerCase().trim())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName().trim())
                    .numeroCni(request.getNumeroCni())
                    .role(UserRole.CHEF_MENAGE)
                    .twoFactorEnabled(false)
                    .twoFactorVerified(false)
                    .menageId(menageUuid)
                    .accountNonLocked(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .enabled(true)
                    .failedAttempts(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            user = userRepository.save(user);
            log.info("Chef de ménage inscrit avec succès: ID={}", user.getId());

            String accessToken  = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return AuthResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .requiresTwoFactor(false)
                    .message("Inscription réussie")
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de l'inscription du chef: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'inscription", e);
        }
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion: {}", maskEmail(request.getEmail()));

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> {
                    log.warn("Échec connexion - email inconnu: {}", maskEmail(request.getEmail()));
                    return AuthException.invalidCredentials();
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw AuthException.invalidCredentials();
        }

        if (!user.isAccountNonLocked()) {
            throw AuthException.accountLocked();
        }

        if (!user.isEnabled()) {
            throw AuthException.accountDisabled();
        }

        user.setFailedAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        if (user.isTwoFactorEnabled()) {
            return handleTwoFactorLogin(user);
        }

        return generateAuthResponse(user, false);
    }

    // ── Connexion Chef de Ménage via NOM + CNI (Front Office Mobile Flutter) ──
    public ChefLoginResponse loginChef(ChefLoginRequest request) {
        log.info("Tentative connexion chef mobile - NOM: {}", request.getNomComplet());

        // Recherche par fullName (insensible à la casse) + CNI + rôle CHEF_MENAGE
        User chef = userRepository.findChefByNomAndCni(
                        request.getNomComplet().trim(),
                        request.getNumeroCni().trim().toUpperCase(),
                        UserRole.CHEF_MENAGE
                )
                .orElseThrow(() -> {
                    log.warn("Échec connexion chef - NOM/CNI introuvable: {}", request.getNomComplet());
                    return AuthException.invalidCredentials();
                });

        // Vérifier que le compte est actif
        if (!chef.isEnabled()) {
            throw AuthException.accountDisabled();
        }

        if (!chef.isAccountNonLocked()) {
            throw AuthException.accountLocked();
        }

        // Vérifier qu'un ménage est bien associé
        if (chef.getMenageId() == null) {
            throw AuthException.userNotFound("Compte non associé à un ménage");
        }

        // Générer les tokens JWT (menageId inclus dans le token via JwtService)
        String accessToken  = jwtService.generateAccessToken(chef);
        String refreshToken = jwtService.generateRefreshToken(chef);

        log.info("Connexion chef réussie: userId={} menageId={}", chef.getId(), chef.getMenageId());

        return ChefLoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .chefId(chef.getId())
                .menageId(chef.getMenageId())
                .nomComplet(chef.getFullName())
                .role("CHEF_MENAGE")
                .twoFactorRequired(false)
                .build();
    }

    public AuthResponse verifyTwoFactor(TwoFaVerifyRequest request) {
        log.info("Vérification 2FA pour l'utilisateur: {}", maskEmail(request.getEmail()));

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> AuthException.userNotFound(request.getEmail()));

        boolean isValid = false;

        if (user.isTwoFactorEnabled() && user.getTwoFactorSecret() != null) {
            isValid = totpManager.verifyCode(user.getTwoFactorSecret(), request.getCode());
        } else {
            String tempCode = tempSecrets.get(user.getId().toString());
            isValid = tempCode != null && tempCode.equals(request.getCode());
            if (isValid) {
                tempSecrets.remove(user.getId().toString());
            }
        }

        if (!isValid) {
            log.warn("Code 2FA invalide pour l'utilisateur: {}", user.getId());
            throw AuthException.invalidTwoFactorCode();
        }

        user.setTwoFactorVerified(true);
        userRepository.save(user);

        log.info("2FA validé avec succès pour l'utilisateur: {}", user.getId());
        return generateAuthResponse(user, false);
    }

    public TwoFaSetupResponse setupTotp(String userId) {
        log.info("Configuration 2FA pour l'utilisateur: {}", userId);

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> AuthException.userNotFound(userId));

        try {
            String secret = totpManager.generateSecret();
            String qrCode = totpManager.getQrCode(secret, user.getEmail());

            tempSecrets.put(userId + "_setup", secret);

            return TwoFaSetupResponse.builder()
                    .secret(secret)
                    .qrCode(qrCode)
                    .message("Scannez le QR code avec Google Authenticator")
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de la génération du secret 2FA", e);
            throw new RuntimeException("Erreur lors de la configuration 2FA", e);
        }
    }

    public void confirmTotpSetup(String userId, String code) {
        log.info("Confirmation 2FA pour l'utilisateur: {}", userId);

        String secret = tempSecrets.get(userId + "_setup");
        if (secret == null) {
            throw new RuntimeException("Aucune configuration 2FA en cours");
        }

        if (!totpManager.verifyCode(secret, code)) {
            throw AuthException.invalidTwoFactorCode();
        }

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> AuthException.userNotFound(userId));

        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        user.setTwoFactorVerified(true);
        userRepository.save(user);

        tempSecrets.remove(userId + "_setup");
        log.info("2FA configuré avec succès pour l'utilisateur: {}", userId);
    }

    public TokenResponse refreshToken(String refreshToken) {
        try {
            String userId = jwtService.extractSubject(refreshToken);
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(AuthException::invalidToken);

            if (!jwtService.validateToken(refreshToken, user)) {
                throw AuthException.invalidToken();
            }

            String newAccessToken  = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400L)
                    .build();

        } catch (Exception e) {
            throw AuthException.invalidToken();
        }
    }

    public void logout(String userId) {
        log.info("Déconnexion de l'utilisateur: {}", userId);
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);

        if (attempts >= 5) {
            user.setAccountNonLocked(false);
            log.warn("Compte verrouillé après {} tentatives échouées: {}", attempts, user.getEmail());
        }

        userRepository.save(user);
    }

    private AuthResponse handleTwoFactorLogin(User user) {
        String tempCode = totpManager.generateTempCode();
        tempSecrets.put(user.getId().toString(), tempCode);

        try {
            emailService.sendTwoFactorCode(user.getEmail(), tempCode);
            log.info("Code 2FA envoyé à l'utilisateur: {}", user.getId());
        } catch (Exception e) {
            log.error("Erreur envoi email 2FA", e);
        }

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .requiresTwoFactor(true)
                .message("Code de vérification envoyé par email")
                .build();
    }

    private AuthResponse generateAuthResponse(User user, boolean requiresTwoFactor) {
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .requiresTwoFactor(requiresTwoFactor)
                .message("Authentification réussie")
                .build();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain    = parts[1];
        if (localPart.length() <= 3) return "***@" + domain;
        return localPart.substring(0, 2) + "***@" + domain;
    }
}
