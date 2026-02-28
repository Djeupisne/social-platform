package com.social.admin.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProgrammeSocialRequest {

    @NotBlank(message = "Le nom du programme est obligatoire")
    private String nom;

    private String description;

    @NotNull @Min(0)
    private Integer scoreMinEligibilite = 0;

    @NotNull
    private Integer scoreMaxEligibilite; // ex: 44 pour cibler vulnérables et très vulnérables

    private LocalDate dateDebut;
    private LocalDate dateFin;

    private Long budgetAlloue;
    private String responsable;
    private String region;
}