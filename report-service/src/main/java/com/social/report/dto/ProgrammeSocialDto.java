package com.social.report.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ProgrammeSocialDto {
    private UUID id;
    private String code;
    private String nom;
    private String description;
    private String type; // "AIDE_DIRECTE", "FORMATION", "EMPLOI", "SANTE", "LOGEMENT"
    private int scoreMaxRequis;
    private int scoreMinRequis; // Parfois un minimum est requis
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut; // "ACTIF", "TERMINE", "EN_PREPARATION"
    private long nombreBeneficiaires;
    private double budgetAlloue;
    private double budgetUtilise;

    // Critères supplémentaires
    private boolean exigeProprietaire;
    private boolean exigeSansEmploi;
    private String regionCiblee;

    // Statistiques d'impact
    private int menagesCibles;
    private int menagesAtteints;
    private double tauxCouverture;
}