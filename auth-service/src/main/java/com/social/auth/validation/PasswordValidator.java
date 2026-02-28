// auth-service/src/main/java/com/social/auth/validation/PasswordValidator.java
package com.social.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.length() < 8) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Le mot de passe doit contenir au moins 8 caractères")
                    .addConstraintViolation();
            return false;
        }

        StringBuilder errors = new StringBuilder();

        if (!password.matches(".*[A-Z].*")) {
            errors.append("- Doit contenir une majuscule\n");
        }
        if (!password.matches(".*[a-z].*")) {
            errors.append("- Doit contenir une minuscule\n");
        }
        if (!password.matches(".*[0-9].*")) {
            errors.append("- Doit contenir un chiffre\n");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errors.append("- Doit contenir un caractère spécial\n");
        }

        if (errors.length() > 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errors.toString())
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}