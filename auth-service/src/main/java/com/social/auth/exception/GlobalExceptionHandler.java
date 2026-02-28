package com.social.auth.exception;

import com.social.auth.dto.response.ValidationErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Gestion des exceptions d'authentification personnalisées
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException e) {
        return buildError(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    // Gestion des erreurs de validation des DTOs (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException e) {

        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        // Récupérer aussi les erreurs globales
        e.getBindingResult().getGlobalErrors()
                .forEach(err -> fieldErrors.put(err.getObjectName(), err.getDefaultMessage()));

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Erreur de validation")
                .fieldErrors(fieldErrors)
                .path(e.getParameter().getExecutable().getName())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // Gestion des violations de contraintes (ex: @ValidEmail, @ValidPassword)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException e) {

        Map<String, String> fieldErrors = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            fieldErrors.put(field, message);
        });

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Violation de contrainte")
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // Mauvaises identifiants (Spring Security)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException e) {
        return buildError(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect");
    }

    // Compte désactivé
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(DisabledException e) {
        return buildError(HttpStatus.FORBIDDEN, "Compte désactivé");
    }

    // Compte verrouillé
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleLocked(LockedException e) {
        return buildError(HttpStatus.FORBIDDEN, "Compte verrouillé");
    }

    // Header manquant (ex: X-User-Id)
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingHeader(
            MissingRequestHeaderException e) {
        return buildError(HttpStatus.BAD_REQUEST,
                "En-tête requis manquant: " + e.getHeaderName());
    }

    // Erreur de format JSON dans la requête
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e) {
        return buildError(HttpStatus.BAD_REQUEST,
                "Format de requête invalide. Vérifiez votre JSON.");
    }

    // Type d'argument incorrect (ex: UUID invalide)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e) {
        String message = String.format("Le paramètre '%s' doit être de type %s",
                e.getName(), e.getRequiredType().getSimpleName());
        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    // Gestion générique des exceptions (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        e.printStackTrace(); // Log pour debug
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue");
    }

    // Méthode utilitaire pour construire les réponses d'erreur
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", message);
        body.put("success", false);
        return ResponseEntity.status(status).body(body);
    }
}