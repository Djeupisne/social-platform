package com.social.auth.dto.response;

import com.social.auth.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private UserRole role;
    private String accessToken;
    private boolean requiresTwoFactor;
    private String refreshToken;
    private boolean twoFactorRequired;
    private String twoFactorType;   // "TOTP" ou "EMAIL_OTP"
    private String tempToken;
    private String message;
    private boolean canSetupTotp;   // true si l'utilisateur peut configurer TOTP
}