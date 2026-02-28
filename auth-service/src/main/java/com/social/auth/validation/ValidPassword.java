// auth-service/src/main/java/com/social/auth/validation/ValidPassword.java
package com.social.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface ValidPassword {
    String message() default "Mot de passe trop faible";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}