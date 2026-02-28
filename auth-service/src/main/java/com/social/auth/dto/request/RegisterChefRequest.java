package com.social.auth.dto.request;

import com.social.auth.validation.ValidEmail;
import com.social.auth.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterChefRequest {

    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le nom ne doit contenir que des lettres")
    private String fullName;

    @NotBlank(message = "L'email est obligatoire")
    @ValidEmail
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @ValidPassword
    private String password;

    @NotBlank(message = "Le numéro CNI est obligatoire")
    @Pattern(regexp = "^[A-Z0-9]{6,15}$", message = "Format CNI invalide")
    private String numeroCni;

    private String menageId; // Optionnel
}