package com.social.main.event;

import com.social.main.enums.CategorieSociale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenageCreatedEvent implements Serializable {
    private UUID menageId;
    private String code;
    private int score;
    private CategorieSociale categorie;
    private String region;
}