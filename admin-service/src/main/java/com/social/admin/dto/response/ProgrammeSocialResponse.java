package com.social.admin.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ProgrammeSocialResponse {
    private UUID id;
    private String nom;
    private String description;
    private int scoreMinEligibilite;
    private int scoreMaxEligibilite;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean actif;
    private Long budgetAlloue;
    private String responsable;
    private String region;
    private long nombreMenagesEligibles;
    private LocalDateTime createdAt;
}