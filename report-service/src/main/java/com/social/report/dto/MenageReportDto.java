package com.social.report.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MenageReportDto {
    private UUID id;
    private String code;
    private String chefNom;
    private String chefCni;
    private String chefContact;
    private String region;
    private String ville;
    private String quartier;

    // Informations pour le scoring
    private int score;
    private String categorie;
    private String categorieLabel;

    // Équipements
    private boolean hasTv;
    private boolean hasRadio;
    private boolean hasMotorcycle;
    private boolean hasCar;
    private String statutHabitation; // "PROPRIETAIRE" ou "LOCATAIRE"
    private boolean isOwner;

    // Résidents
    private int nombreResidents;
    private double maxSalary;
    private double salaireMoyen;

    // Dates
    private LocalDateTime dateCreation;
    private LocalDateTime derniereMaj;

    // Méthodes utilitaires pour l'affichage
    public String getStatutHabitationLibelle() {
        return isOwner ? "Propriétaire" : "Locataire";
    }

    public String getEquipements() {
        StringBuilder sb = new StringBuilder();
        if (hasTv) sb.append("TV ");
        if (hasRadio) sb.append("Radio ");
        if (hasMotorcycle) sb.append("Moto ");
        if (hasCar) sb.append("Voiture");
        return sb.toString().trim();
    }
}