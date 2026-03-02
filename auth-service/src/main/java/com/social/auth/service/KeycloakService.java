package com.social.auth.service;

import com.social.auth.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service pour gérer les utilisateurs dans Keycloak.
 * Auth-service reste le point d'entrée pour l'authentification,
 * Keycloak gère les rôles et permissions.
 */
@Service
public class KeycloakService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);

    @Value("${keycloak.server-url:http://keycloak:8080}")
    private String keycloakUrl;

    @Value("${keycloak.realm:social-togo}")
    private String realm;

    @Value("${keycloak.client-id:social-platform}")
    private String clientId;

    @Value("${keycloak.client-secret:social-platform-secret-2024}")
    private String clientSecret;

    @Value("${keycloak.admin-username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin-password:Admin@Keycloak2024!}")
    private String adminPassword;

    private final RestTemplate restTemplate = new RestTemplate();

    // ─── Token Admin ─────────────────────────────────────────────────────────

    private String getAdminToken() {
        String tokenUrl = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli");
        body.add("username", adminUsername);
        body.add("password", adminPassword);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tokenUrl,
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            return (String) response.getBody().get("access_token");
        } catch (Exception e) {
            log.error("Impossible d'obtenir le token admin Keycloak: {}", e.getMessage());
            return null;
        }
    }

    // ─── Créer utilisateur dans Keycloak ─────────────────────────────────────

    public String createKeycloakUser(String username, String email, String password, UserRole role) {
        try {
            String adminToken = getAdminToken();
            if (adminToken == null) {
                log.warn("Keycloak non disponible - utilisateur créé localement seulement");
                return null;
            }

            String usersUrl = keycloakUrl + "/admin/realms/" + realm + "/users";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            Map<String, Object> userBody = Map.of(
                    "username", username,
                    "email", email,
                    "enabled", true,
                    "emailVerified", true,
                    "credentials", List.of(Map.of(
                            "type", "password",
                            "value", password,
                            "temporary", false
                    ))
            );

            ResponseEntity<Void> response = restTemplate.postForEntity(
                    usersUrl,
                    new HttpEntity<>(userBody, headers),
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                String location = response.getHeaders().getFirst("Location");
                String keycloakUserId = location != null ? location.substring(location.lastIndexOf("/") + 1) : null;

                if (keycloakUserId != null) {
                    assignRole(adminToken, keycloakUserId, role.name());
                    log.info("Utilisateur {} créé dans Keycloak avec rôle {}", username, role);
                }
                return keycloakUserId;
            }
        } catch (Exception e) {
            log.error("Erreur création utilisateur Keycloak: {}", e.getMessage());
        }
        return null;
    }

    // ─── Assigner un rôle ────────────────────────────────────────────────────

    private void assignRole(String adminToken, String userId, String roleName) {
        try {
            // 1. Récupérer l'ID du rôle
            String roleUrl = keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);

            ResponseEntity<Map> roleResponse = restTemplate.exchange(
                    roleUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class
            );

            if (roleResponse.getStatusCode() != HttpStatus.OK) return;

            Map<String, Object> roleData = roleResponse.getBody();

            // 2. Assigner le rôle
            String assignUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.exchange(
                    assignUrl, HttpMethod.POST,
                    new HttpEntity<>(List.of(roleData), headers),
                    Void.class
            );

            log.info("Rôle {} assigné à l'utilisateur {}", roleName, userId);
        } catch (Exception e) {
            log.error("Erreur assignation rôle: {}", e.getMessage());
        }
    }

    // ─── Obtenir token Keycloak pour un utilisateur ───────────────────────────

    public Map<String, Object> getUserToken(String username, String password) {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", username);
        body.add("password", password);
        body.add("scope", "openid profile email");

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tokenUrl,
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Erreur obtention token Keycloak: {}", e.getMessage());
            return null;
        }
    }

    // ─── Supprimer utilisateur ────────────────────────────────────────────────

    public void deleteKeycloakUser(String keycloakUserId) {
        try {
            String adminToken = getAdminToken();
            if (adminToken == null) return;

            String deleteUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);

            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
            log.info("Utilisateur {} supprimé de Keycloak", keycloakUserId);
        } catch (Exception e) {
            log.error("Erreur suppression utilisateur Keycloak: {}", e.getMessage());
        }
    }

    // ─── Changer le mot de passe ──────────────────────────────────────────────

    public void resetPassword(String keycloakUserId, String newPassword) {
        try {
            String adminToken = getAdminToken();
            if (adminToken == null) return;

            String resetUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/reset-password";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "type", "password",
                    "value", newPassword,
                    "temporary", false
            );

            restTemplate.exchange(resetUrl, HttpMethod.PUT, new HttpEntity<>(body, headers), Void.class);
            log.info("Mot de passe réinitialisé pour l'utilisateur {}", keycloakUserId);
        } catch (Exception e) {
            log.error("Erreur réinitialisation mot de passe: {}", e.getMessage());
        }
    }
}