package com.social.admin.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class MenageEligibleResponse {
    private UUID id;
    private String code;
    private int score;
    private String categorie;
    private String region;
    private String ville;
    private String nomChef;
}