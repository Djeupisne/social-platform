package com.social.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class StatistiquesResponse {
    private long totalMenages;
    private long totalResidents;
    private Map<String, Long> menagesParCategorie;
    private Map<String, Long> menagesParRegion;
    private double scoresMoyen;
}