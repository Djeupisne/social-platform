package com.social.report.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class StatistiquesDto {
    private long totalMenages;
    private long totalResidents;
    private long totalAgents;

    // Distribution par catégorie
    private Map<String, Long> menagesParCategorie;
    private Map<String, Double> pourcentageParCategorie;

    // Scores
    private double scoreMoyenGlobal;
    private int scoreMin;
    private int scoreMax;
    private double ecartTypeScore;

    // Équipements
    private long menagesAvecTv;
    private long menagesAvecRadio;
    private long menagesAvecMoto;
    private long menagesAvecVoiture;
    private long menagesProprietaires;

    // Taux (en pourcentage)
    private double tauxEquipementTv;
    private double tauxEquipementRadio;
    private double tauxEquipementMoto;
    private double tauxEquipementVoiture;
    private double tauxProprietaires;

    // Vulnérabilité
    private long nombreTresVulnerables; // score < 20
    private long nombreVulnerables;      // score 20-39
    private long nombreMoyens;            // score 40-59
    private long nombreAises;             // score 60-84
    private long nombreTresRiches;        // score > 85

    // Distribution géographique
    private Map<String, Long> menagesParRegion;
    private Map<String, Double> scoreMoyenParRegion;

    // Évolution temporelle
    private Map<String, Long> inscriptionsParMois;
    private Map<String, Double> evolutionScoreMoyen;

    private LocalDateTime dateGeneration;

    // Méthodes utilitaires
    public long getTotalMenagesVulnerables() {
        return nombreTresVulnerables + nombreVulnerables;
    }

    public double getTauxVulnerabilite() {
        return totalMenages > 0 ?
                (double) getTotalMenagesVulnerables() / totalMenages * 100 : 0;
    }
}