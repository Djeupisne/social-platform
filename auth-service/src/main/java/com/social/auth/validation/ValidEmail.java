// auth-service/src/main/java/com/social/auth/validation/ValidEmail.java
package com.social.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
@Documented
public @interface ValidEmail {
    String message() default "Format d'email invalide";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}