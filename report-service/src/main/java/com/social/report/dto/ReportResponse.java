package com.social.report.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private String reportId;
    private String reportName;
    private String format;
    private LocalDateTime generatedAt;
    private long fileSize;
    private byte[] content;       // Contenu du fichier (Excel, PDF, CSV)
    private String downloadUrl;    // URL de téléchargement
    private String message;
    private boolean success;

    // Métadonnées
    private int totalRecords;
    private String filtreRegion;
    private String filtreCategorie;
    private LocalDateTime periodeDebut;
    private LocalDateTime periodeFin;
}