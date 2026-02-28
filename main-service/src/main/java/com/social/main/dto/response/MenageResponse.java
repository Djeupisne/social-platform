package com.social.main.dto.response;

import com.social.main.enums.CategorieSociale;
import com.social.main.enums.StatutHabitation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MenageResponse {
    private UUID id;
    private String code;
    private boolean aTelevision;
    private boolean aRadio;
    private boolean aMoto;
    private boolean aVoiture;
    private StatutHabitation statutHabitation;
    private int score;
    private CategorieSociale categorie;
    private String categorieLabel;
    private String region;
    private String ville;
    private String quartier;
    private String adresse;
    private int nombreResidents;
    private String nomChef;
    private LocalDateTime createdAt;
}