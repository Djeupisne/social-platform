package com.social.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class AuthenticationFilter implements GatewayFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/2fa/verify",
            "/api/auth/chef/login",
            "/api/auth/chef/register",
            "/actuator/health",
            "/actuator/info"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        if (!request.getHeaders().containsKey("Authorization")) {
            log.warn("Missing Authorization header for path: {}", path);
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            // Decode JWT payload (works for both Keycloak and custom JWT)
            TokenInfo tokenInfo = decodeJwtPayload(token);

            if (tokenInfo == null) {
                log.warn("Invalid token format");
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            log.debug("Token validated for user: {} with role: {}", tokenInfo.userId, tokenInfo.role);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", tokenInfo.userId != null ? tokenInfo.userId : "")
                    .header("X-User-Role", tokenInfo.role != null ? tokenInfo.role : "")
                    .header("X-User-Email", tokenInfo.email != null ? tokenInfo.email : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Decode JWT payload without signature verification.
     * Signature is verified by Keycloak's JWKS in each microservice.
     */
    private TokenInfo decodeJwtPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payload = new String(
                    Base64.getUrlDecoder().decode(addPadding(parts[1])),
                    StandardCharsets.UTF_8
            );

            JsonNode claims = objectMapper.readTree(payload);

            TokenInfo info = new TokenInfo();

            // Subject = user ID (works for both Keycloak and custom JWT)
            info.userId = claims.has("sub") ? claims.get("sub").asText() : null;
            info.email = claims.has("email") ? claims.get("email").asText() : null;

            // Extract role - handles both Keycloak format and custom JWT format
            if (claims.has("roles") && claims.get("roles").isArray()) {
                // Keycloak format: "roles": ["AGENT", "default-roles-social-togo"]
                for (JsonNode roleNode : claims.get("roles")) {
                    String role = roleNode.asText();
                    if (role.equals("AGENT") || role.equals("CHEF_MENAGE") || role.equals("SUPER_ADMIN")) {
                        info.role = role;
                        break;
                    }
                }
            } else if (claims.has("role")) {
                // Custom JWT format: "role": "AGENT"
                info.role = claims.get("role").asText();
            } else if (claims.has("realm_access")) {
                // Keycloak alternative format
                JsonNode realmAccess = claims.get("realm_access");
                if (realmAccess.has("roles")) {
                    for (JsonNode roleNode : realmAccess.get("roles")) {
                        String role = roleNode.asText();
                        if (role.equals("AGENT") || role.equals("CHEF_MENAGE") || role.equals("SUPER_ADMIN")) {
                            info.role = role;
                            break;
                        }
                    }
                }
            }

            // Check token expiration
            if (claims.has("exp")) {
                long exp = claims.get("exp").asLong();
                if (System.currentTimeMillis() / 1000 > exp) {
                    log.warn("Token expired");
                    return null;
                }
            }

            return info;

        } catch (Exception e) {
            log.error("Error decoding JWT: {}", e.getMessage());
            return null;
        }
    }

    private String addPadding(String base64) {
        int padding = 4 - base64.length() % 4;
        if (padding < 4) {
            base64 += "=".repeat(padding);
        }
        return base64;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    private static class TokenInfo {
        String userId;
        String email;
        String role;
    }
}