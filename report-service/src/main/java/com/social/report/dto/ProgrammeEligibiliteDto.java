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

    // Méthodes getter explicites (si Lombok ne fonctionne pas)
    public String getNomProgramme() { return nomProgramme; }
    public String getDescription() { return description; }
    public LocalDateTime getDateGeneration() { return dateGeneration; }
    public int getTotalMenagesEligibles() { return totalMenagesEligibles; }
    public List<MenageEligibleDto> getMenagesEligibles() { return menagesEligibles; }
}