package com.social.main.dto.request;

import com.social.main.enums.NiveauDiplome;
import com.social.main.enums.TrancheSalariale;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ResidentRequest {

    @NotBlank(message = "Le numéro CNI est obligatoire")
    private String numeroCni;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    private String nationalite;

    @NotNull(message = "Le niveau de diplôme est obligatoire")
    private NiveauDiplome niveauDiplome;

    @NotNull(message = "La tranche salariale est obligatoire")
    private TrancheSalariale trancheSalariale;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    private String telephone;

    private boolean chef = false;
}