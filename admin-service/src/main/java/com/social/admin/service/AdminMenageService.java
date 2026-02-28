package com.social.admin.service;

import com.social.admin.dto.response.StatistiquesResponse;
import com.social.admin.feign.MainServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminMenageService {

    private final MainServiceClient mainServiceClient;

    public StatistiquesResponse getStatistiques() {
        // Dans une implémentation complète, on appellerait main-service
        // pour agréger les statistiques
        return StatistiquesResponse.builder()
                .totalMenages(0L) // à compléter via Feign
                .totalResidents(0L)
                .menagesParCategorie(Map.of(
                        "TRES_VULNERABLE", 0L,
                        "VULNERABLE", 0L,
                        "A_RISQUE", 0L,
                        "NON_VULNERABLE", 0L,
                        "RICHE", 0L,
                        "TRES_RICHE", 0L
                ))
                .menagesParRegion(Map.of())
                .scoresMoyen(0.0)
                .build();
    }
}