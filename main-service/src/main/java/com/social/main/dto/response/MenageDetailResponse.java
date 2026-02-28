package com.social.main.dto.response;

import com.social.main.enums.CategorieSociale;
import com.social.main.enums.StatutHabitation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class MenageDetailResponse {
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
    private List<ResidentResponse> residents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}