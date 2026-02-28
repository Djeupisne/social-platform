package com.social.report.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProgrammeEligibiliteDto {
    private UUID programmeId;
    private String nomProgramme;
    private String description;
    private int scoreMaxRequis;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private List<MenageEligibleDto> menagesEligibles;
    private int totalMenagesEligibles;
    private LocalDateTime dateGeneration;

    // Statistiques rapides
    private int totalMenagesParRegion;
    private double scoreMoyenEligibles;
}

@Data
@Builder
class MenageEligibleDto {
    private UUID menageId;
    private String codeMenage;
    private String chefNom;
    private String chefContact;
    private int score;
    private String categorie;
    private String region;
    private String ville;
    private int nombreResidents;
    private boolean beneficiaireActuel; // Déjà bénéficiaire ou non
}