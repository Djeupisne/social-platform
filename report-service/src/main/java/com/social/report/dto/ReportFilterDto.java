package com.social.report.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ReportFilterDto {
    private List<String> regions;
    private List<String> categories;
    private List<String> villes;
    private LocalDate dateCreationDebut;
    private LocalDate dateCreationFin;
    private LocalDate dateMajDebut;
    private LocalDate dateMajFin;
    private Integer scoreMin;
    private Integer scoreMax;
    private Boolean proprietaire;
    private Boolean hasTv;
    private Boolean hasRadio;
    private Boolean hasMoto;
    private Boolean hasVoiture;
    private Integer nombreResidentsMin;
    private Integer nombreResidentsMax;
    private Double salaireMin;
    private Double salaireMax;
    private String triPar; // "score", "date", "region"
    private String ordreTri; // "ASC", "DESC"
    private Integer page;
    private Integer taille;
}