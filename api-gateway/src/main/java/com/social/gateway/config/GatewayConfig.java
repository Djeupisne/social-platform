package com.social.gateway.config;

import com.social.gateway.filter.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authFilter;

    public GatewayConfig(AuthenticationFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service - public routes (no auth required)
                .route("auth-service-public", r -> r
                        .path("/api/v1/auth/**")
                        .uri("lb://auth-service"))

                // Main Service - protected
                .route("main-service", r -> r
                        .path("/api/v1/menages/**", "/api/v1/residents/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://main-service"))

                // Scoring Service - protected
                .route("scoring-service", r -> r
                        .path("/api/v1/scoring/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://scoring-service"))

                // Admin Service - protected (AGENT role required)
                .route("admin-service", r -> r
                        .path("/api/v1/admin/**", "/api/v1/programmes/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://admin-service"))

                // Report Service - protected
                .route("report-service", r -> r
                        .path("/api/v1/reports/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://report-service"))

                .build();
    }
}