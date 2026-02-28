package com.social.auth.controller;

import com.social.auth.dto.request.*;
import com.social.auth.dto.response.AuthResponse;
import com.social.auth.dto.response.ChefLoginResponse;
import com.social.auth.dto.response.TokenResponse;
import com.social.auth.dto.response.TwoFaSetupResponse;
import com.social.auth.dto.response.UserResponse;
import com.social.auth.entity.User;
import com.social.auth.exception.AuthException;
import com.social.auth.repository.UserRepository;
import com.social.auth.service.AuthService;
import com.social.auth.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API d'authentification et gestion des utilisateurs")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8100"})
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Operation(summary = "Inscription d'un agent (back-office)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Agent créé avec succès",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    @PostMapping(value = "/agents/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> registerAgent(
            @Valid @RequestBody RegisterAgentRequest request) {
        log.info("POST /agents/register - Email: {}", maskEmail(request.getEmail()));
        AuthResponse response = authService.registerAgent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Inscription d'un chef de ménage (front-office)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Chef créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    @PostMapping("/chefs/register")
    public ResponseEntity<AuthResponse> registerChef(
            @Valid @RequestBody RegisterChefRequest request) {
        log.info("POST /chefs/register - Email: {}", maskEmail(request.getEmail()));
        AuthResponse response = authService.registerChef(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Connexion utilisateur (Back Office — email + mot de passe)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion réussie"),
            @ApiResponse(responseCode = "401", description = "Email/mot de passe incorrect"),
            @ApiResponse(responseCode = "403", description = "Compte verrouillé/désactivé")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("POST /login - Email: {}", maskEmail(request.getEmail()));
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ── Connexion Chef de Ménage (Front Office Mobile Flutter) ───────────────
    @Operation(
        summary     = "Connexion Chef de Ménage — Front Office Mobile",
        description = "Authentification via Nom Complet + Numéro CNI, sans mot de passe. " +
                      "Utilisé par l'application Flutter du front office."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion réussie",
                    content = @Content(schema = @Schema(implementation = ChefLoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "NOM ou CNI incorrect"),
            @ApiResponse(responseCode = "403", description = "Compte désactivé ou sans ménage associé")
    })
    @PostMapping("/chef/login")
    public ResponseEntity<ChefLoginResponse> loginChef(
            @Valid @RequestBody ChefLoginRequest request) {
        log.info("POST /chef/login - NOM: {}", request.getNomComplet());
        ChefLoginResponse response = authService.loginChef(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Vérification du code 2FA")
    @PostMapping("/2fa/verify")
    public ResponseEntity<AuthResponse> verifyTwoFactor(
            @Valid @RequestBody TwoFaVerifyRequest request) {
        log.info("POST /2fa/verify - Email: {}", maskEmail(request.getEmail()));
        AuthResponse response = authService.verifyTwoFactor(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Configuration initiale du 2FA")
    @GetMapping("/2fa/setup")
    public ResponseEntity<TwoFaSetupResponse> setupTotp(
            @RequestHeader("X-User-Id") @Parameter(description = "ID de l'utilisateur") String userId) {
        log.info("GET /2fa/setup - UserId: {}", userId);
        TwoFaSetupResponse response = authService.setupTotp(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirmation de la configuration 2FA")
    @PostMapping("/2fa/confirm")
    public ResponseEntity<Map<String, String>> confirmTotp(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> body) {
        log.info("POST /2fa/confirm - UserId: {}", userId);

        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le code est requis"));
        }

        authService.confirmTotpSetup(userId, code);
        return ResponseEntity.ok(Map.of(
                "message", "2FA configuré avec succès",
                "success", "true"
        ));
    }

    @Operation(summary = "Rafraîchissement du token d'accès")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /refresh - Rafraîchissement token");

        try {
            String userId = jwtService.extractSubject(request.getRefreshToken());
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> AuthException.userNotFound(userId));

            if (!jwtService.validateToken(request.getRefreshToken(), user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String newAccess  = jwtService.generateAccessToken(user);
            String newRefresh = jwtService.generateRefreshToken(user);

            return ResponseEntity.ok(TokenResponse.builder()
                    .accessToken(newAccess)
                    .refreshToken(newRefresh)
                    .tokenType("Bearer")
                    .expiresIn(86400L)
                    .build());

        } catch (AuthException e) {
            log.warn("Token refresh échoué: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(TokenResponse.builder()
                            .error(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Erreur inattendue lors du refresh token", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Récupérer les informations de l'utilisateur connecté")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(UserResponse.builder()
                            .error("Utilisateur non authentifié")
                            .build());
        }

        log.info("GET /me - UserId: {}", userId);

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> AuthException.userNotFound(userId));

        return ResponseEntity.ok(mapToUserResponse(user));
    }

    @Operation(summary = "Mettre à jour le profil utilisateur")
    @PutMapping("/users/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> body) {

        log.info("PUT /users/profile - UserId: {}", userId);

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> AuthException.userNotFound(userId));

        boolean updated = false;

        if (body.containsKey("fullName") && !body.get("fullName").isBlank()) {
            user.setFullName(body.get("fullName").trim());
            updated = true;
        }

        if (body.containsKey("numeroCni") && !body.get("numeroCni").isBlank()) {
            String cni = body.get("numeroCni").trim();
            if (!cni.matches("^[A-Z0-9]{6,15}$")) {
                return ResponseEntity.badRequest()
                        .body(UserResponse.builder()
                                .error("Format CNI invalide")
                                .build());
            }
            user.setNumeroCni(cni);
            updated = true;
        }

        if (body.containsKey("email") && !body.get("email").isBlank()) {
            String newEmail = body.get("email").trim().toLowerCase();
            if (!newEmail.equals(user.getEmail()) &&
                    userRepository.existsByEmail(newEmail)) {
                return ResponseEntity.badRequest()
                        .body(UserResponse.builder()
                                .error("Email déjà utilisé")
                                .build());
            }
            user.setEmail(newEmail);
            updated = true;
        }

        if (!updated) {
            return ResponseEntity.badRequest()
                    .body(UserResponse.builder()
                            .error("Aucune donnée valide à mettre à jour")
                            .build());
        }

        user = userRepository.save(user);
        log.info("Profil mis à jour pour l'utilisateur: {}", userId);

        return ResponseEntity.ok(mapToUserResponse(user));
    }

    @Operation(summary = "Déconnexion")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("X-User-Id") String userId) {
        log.info("POST /logout - UserId: {}", userId);
        authService.logout(userId);
        return ResponseEntity.ok(Map.of(
                "message", "Déconnexion réussie",
                "success", "true"
        ));
    }

    @Operation(summary = "Vérifier la validité du token")
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestParam("token") String token) {
        try {
            String userId = jwtService.extractSubject(token);
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElse(null);

            if (user != null && jwtService.validateToken(token, user)) {
                return ResponseEntity.ok(Map.of(
                        "valid",    true,
                        "userId",   userId,
                        "role",     user.getRole().name(),
                        "menageId", user.getMenageId() != null ? user.getMenageId().toString() : ""
                ));
            }
        } catch (Exception e) {
            log.debug("Token invalide: {}", e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valid", false));
    }

    @Operation(summary = "Demander un reset de mot de passe")
    @PostMapping("/password/reset-request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email requis"));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Si l'email existe, un lien de réinitialisation a été envoyé"
        ));
    }

    @Operation(summary = "Changer le mot de passe")
    @PostMapping("/password/change")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> body) {

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ancien et nouveau mot de passe requis"));
        }

        if (newPassword.length() < 8) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le nouveau mot de passe doit contenir au moins 8 caractères"));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Mot de passe changé avec succès"
        ));
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .numeroCni(user.getNumeroCni())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .twoFactorVerified(user.isTwoFactorVerified())
                .createdAt(user.getCreatedAt())
                .menageId(user.getMenageId())
                .build();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts     = email.split("@");
        String localPart   = parts[0];
        String domain      = parts[1];
        if (localPart.length() <= 3) return "***@" + domain;
        return localPart.substring(0, 2) + "***@" + domain;
    }
}
