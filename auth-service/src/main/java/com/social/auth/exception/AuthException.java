package com.social.auth.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private final String errorCode;

    public AuthException(String message) {
        super(message);
        this.errorCode = "AUTH_ERROR";
    }

    public AuthException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTH_ERROR";
    }

    // Méthodes statiques pour créer facilement des exceptions
    public static AuthException invalidCredentials() {
        return new AuthException("Email ou mot de passe incorrect", "INVALID_CREDENTIALS");
    }

    public static AuthException userNotFound(String email) {
        return new AuthException("Aucun utilisateur trouvé avec l'email: " + email, "USER_NOT_FOUND");
    }

    public static AuthException emailAlreadyExists(String email) {
        return new AuthException("L'email " + email + " est déjà utilisé", "EMAIL_EXISTS");
    }

    public static AuthException invalidTwoFactorCode() {
        return new AuthException("Code 2FA invalide", "INVALID_2FA_CODE");
    }

    public static AuthException twoFactorRequired() {
        return new AuthException("Code 2FA requis", "2FA_REQUIRED");
    }

    public static AuthException accountLocked() {
        return new AuthException("Compte temporairement verrouillé", "ACCOUNT_LOCKED");
    }

    public static AuthException accountDisabled() {
        return new AuthException("Compte désactivé", "ACCOUNT_DISABLED");
    }

    public static AuthException invalidToken() {
        return new AuthException("Token invalide ou expiré", "INVALID_TOKEN");
    }

    public static AuthException invalidCni() {
        return new AuthException("Numéro CNI invalide", "INVALID_CNI");
    }
}