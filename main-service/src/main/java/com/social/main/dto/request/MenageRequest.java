package com.social.main.dto.request;

import com.social.main.enums.StatutHabitation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MenageRequest {

    private boolean aTelevision;
    private boolean aRadio;
    private boolean aMoto;
    private boolean aVoiture;

    @NotNull(message = "Le statut d'habitation est obligatoire")
    private StatutHabitation statutHabitation;

    private String region;
    private String ville;
    private String quartier;
    private String adresse;

    @NotEmpty(message = "Le ménage doit avoir au moins un résident (le chef)")
    @Valid
    private List<ResidentRequest> residents;
}