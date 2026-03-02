package com.social.report.enums;

public enum TypeRapport {
    MENAGES("Rapport des ménages"),
    ELIGIBILITE("Rapport d'éligibilité"),
    STATISTIQUES("Rapport statistique"),
    TOP_MENAGES("Top ménages"),
    VULNERABLES("Ménages vulnérables");

    private final String libelle;

    TypeRapport(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}