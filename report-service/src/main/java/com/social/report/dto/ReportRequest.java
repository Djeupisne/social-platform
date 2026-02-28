package com.social.report.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReportRequest {
    private String region;
    private String categorie;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String format = "EXCEL"; // EXCEL, CSV, PDF

    // Méthode utilitaire pour vérifier si c'est un format valide
    public boolean isValidFormat() {
        return "EXCEL".equalsIgnoreCase(format) ||
                "CSV".equalsIgnoreCase(format) ||
                "PDF".equalsIgnoreCase(format);
    }
}