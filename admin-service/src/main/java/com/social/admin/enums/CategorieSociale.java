package com.social.scoring.enums;

import lombok.Getter;

@Getter
public enum CategorieSociale {
    TRES_VULNERABLE("Très vulnérable", 0, 19),
    VULNERABLE("Vulnérable", 20, 44),
    A_RISQUE("À risque", 45, 55),
    NON_VULNERABLE("Non vulnérable", 56, 70),
    RICHE("Riche", 71, 85),
    TRES_RICHE("Très riche", 86, Integer.MAX_VALUE);

    private final String label;
    private final int scoreMin;
    private final int scoreMax;

    CategorieSociale(String label, int scoreMin, int scoreMax) {
        this.label = label;
        this.scoreMin = scoreMin;
        this.scoreMax = scoreMax;
    }

    public static CategorieSociale fromScore(int score) {
        for (CategorieSociale cat : values()) {
            if (score >= cat.scoreMin && score <= cat.scoreMax) return cat;
        }
        return TRES_RICHE;
    }
}