package com.social.report.enums;

public enum CategorieMenage {
    TRES_VULNERABLE("Très vulnérable"),
    VULNERABLE("Vulnérable"),
    MOYEN("Moyen"),
    AISE("Aisé"),
    TRES_RICHE("Très riche");

    private final String label;

    CategorieMenage(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}