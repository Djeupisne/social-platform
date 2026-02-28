// src/main/java/com/social/auth/validation/ValidFullName.java
package com.social.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Size(min = 3, max = 100)
@Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$")
@Documented
public @interface ValidFullName {
    String message() default "Nom invalide";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}