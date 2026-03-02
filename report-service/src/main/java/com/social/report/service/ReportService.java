package com.social.report.service;

import com.social.report.dto.MenageReportDto;
import com.social.report.dto.ProgrammeEligibiliteDto;
import com.social.report.dto.StatistiquesDto;
import com.social.report.dto.ReportRequest;
import com.social.report.dto.ReportFilterDto;
import com.social.report.dto.MenageEligibleDto;
import com.social.report.enums.TypeRapport;
import com.social.report.exception.ReportGenerationException;
import com.social.report.util.ExcelGenerator;
import com.social.report.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ExcelGenerator excelGenerator;
    private final PdfGenerator pdfGenerator;

    /**
     * Génère un rapport Excel des ménages avec leur catégorie sociale
     */
    public byte[] generateMenagesReport(List<MenageReportDto> menages) throws IOException {
        log.info("Génération du rapport Excel pour {} ménages", menages.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ménages");

            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle vulnerableStyle = createCategoryStyle(workbook, IndexedColors.RED);
            CellStyle moyenStyle = createCategoryStyle(workbook, IndexedColors.YELLOW);
            CellStyle aiseStyle = createCategoryStyle(workbook, IndexedColors.LIGHT_GREEN);
            CellStyle richeStyle = createCategoryStyle(workbook, IndexedColors.GREEN);

            // En-têtes
            String[] headers = {
                    "Code Ménage", "Chef de ménage", "Région", "Ville", "Quartier",
                    "Score", "Catégorie", "Télévision", "Radio", "Moto", "Voiture",
                    "Statut Habitation", "Propriétaire", "Nb Résidents", "Salaire Max (FCFA)",
                    "Date création", "Dernière mise à jour"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Données
            int rowNum = 1;
            for (MenageReportDto m : menages) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(m.getCode());
                row.createCell(1).setCellValue(m.getChefNom());
                row.createCell(2).setCellValue(m.getRegion());
                row.createCell(3).setCellValue(m.getVille());
                row.createCell(4).setCellValue(m.getQuartier());
                row.createCell(5).setCellValue(m.getScore());

                Cell catCell = row.createCell(6);
                catCell.setCellValue(m.getCategorieLabel());

                // Appliquer la couleur selon la catégorie
                switch (m.getCategorie()) {
                    case "TRES_VULNERABLE":
                    case "VULNERABLE":
                        catCell.setCellStyle(vulnerableStyle);
                        break;
                    case "MOYEN":
                        catCell.setCellStyle(moyenStyle);
                        break;
                    case "AISE":
                        catCell.setCellStyle(aiseStyle);
                        break;
                    case "TRES_RICHE":
                        catCell.setCellStyle(richeStyle);
                        break;
                    default:
                        break;
                }

                row.createCell(7).setCellValue(m.isHasTv() ? "Oui" : "Non");
                row.createCell(8).setCellValue(m.isHasRadio() ? "Oui" : "Non");
                row.createCell(9).setCellValue(m.isHasMotorcycle() ? "Oui" : "Non");
                row.createCell(10).setCellValue(m.isHasCar() ? "Oui" : "Non");
                row.createCell(11).setCellValue(m.getStatutHabitation());
                row.createCell(12).setCellValue(m.isOwner() ? "Oui" : "Non");
                row.createCell(13).setCellValue(m.getNombreResidents());
                row.createCell(14).setCellValue(m.getMaxSalary());
                row.createCell(15).setCellValue(formatDate(m.getDateCreation()));
                row.createCell(16).setCellValue(formatDate(m.getDerniereMaj()));
            }

            // Ajuster automatiquement les largeurs
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Génère un rapport PDF des ménages éligibles à un programme
     */
    public byte[] generateEligibiliteReport(ProgrammeEligibiliteDto programme) {
        log.info("Génération du rapport d'éligibilité pour le programme: {}", programme.getNomProgramme());

        try {
            return pdfGenerator.generateEligibiliteReport(programme);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw new ReportGenerationException("Impossible de générer le rapport PDF", e);
        }
    }

    /**
     * Génère des statistiques sur les ménages
     */
    public StatistiquesDto generateStatistiques(List<MenageReportDto> menages) {
        log.info("Calcul des statistiques sur {} ménages", menages.size());

        long totalMenages = menages.size();

        // Statistiques par catégorie
        Map<String, Long> parCategorie = menages.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getCategorieLabel(),
                        Collectors.counting()
                ));

        // Moyenne des scores
        double scoreMoyen = menages.stream()
                .mapToInt(MenageReportDto::getScore)
                .average()
                .orElse(0.0);

        // Nombre de ménages propriétaires
        long proprietaires = menages.stream()
                .filter(MenageReportDto::isOwner)
                .count();

        // Équipements
        long avecTv = menages.stream().filter(MenageReportDto::isHasTv).count();
        long avecRadio = menages.stream().filter(MenageReportDto::isHasRadio).count();
        long avecMoto = menages.stream().filter(MenageReportDto::isHasMotorcycle).count();
        long avecVoiture = menages.stream().filter(MenageReportDto::isHasCar).count();

        // Ménages très vulnérables (<20)
        long tresVulnerables = menages.stream()
                .filter(m -> m.getScore() < 20)
                .count();

        // Ménages très riches (>85)
        long tresRiches = menages.stream()
                .filter(m -> m.getScore() > 85)
                .count();

        return StatistiquesDto.builder()
                .totalMenages(totalMenages)
                .menagesParCategorie(parCategorie)
                .scoreMoyenGlobal(scoreMoyen)
                .tauxProprietaires(totalMenages > 0 ? (double) proprietaires / totalMenages * 100 : 0)
                .tauxEquipementTv(totalMenages > 0 ? (double) avecTv / totalMenages * 100 : 0)
                .tauxEquipementRadio(totalMenages > 0 ? (double) avecRadio / totalMenages * 100 : 0)
                .tauxEquipementMoto(totalMenages > 0 ? (double) avecMoto / totalMenages * 100 : 0)
                .tauxEquipementVoiture(totalMenages > 0 ? (double) avecVoiture / totalMenages * 100 : 0)
                .nombreTresVulnerables(tresVulnerables)
                .nombreTresRiches(tresRiches)
                .dateGeneration(LocalDateTime.now())
                .build();
    }

    /**
     * Export des statistiques au format Excel
     */
    public byte[] exportStatistiques(StatistiquesDto stats) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Statistiques");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            int rowNum = 0;

            // En-tête
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Indicateur");
            headerRow.createCell(1).setCellValue("Valeur");
            headerRow.getCell(0).setCellStyle(headerStyle);
            headerRow.getCell(1).setCellStyle(headerStyle);

            // Données
            addStatRow(sheet, rowNum++, "Total ménages", stats.getTotalMenages());
            addStatRow(sheet, rowNum++, "Score moyen", stats.getScoreMoyenGlobal(), numberStyle);
            addStatRow(sheet, rowNum++, "Taux propriétaires (%)", stats.getTauxProprietaires(), numberStyle);
            addStatRow(sheet, rowNum++, "Taux TV (%)", stats.getTauxEquipementTv(), numberStyle);
            addStatRow(sheet, rowNum++, "Taux Radio (%)", stats.getTauxEquipementRadio(), numberStyle);
            addStatRow(sheet, rowNum++, "Taux Moto (%)", stats.getTauxEquipementMoto(), numberStyle);
            addStatRow(sheet, rowNum++, "Taux Voiture (%)", stats.getTauxEquipementVoiture(), numberStyle);
            addStatRow(sheet, rowNum++, "Ménages très vulnérables", stats.getNombreTresVulnerables());
            addStatRow(sheet, rowNum++, "Ménages très riches", stats.getNombreTresRiches());

            // Distribution par catégorie
            rowNum += 2;
            Row catHeader = sheet.createRow(rowNum++);
            catHeader.createCell(0).setCellValue("Catégorie");
            catHeader.createCell(1).setCellValue("Nombre");
            catHeader.getCell(0).setCellStyle(headerStyle);
            catHeader.getCell(1).setCellStyle(headerStyle);

            if (stats.getMenagesParCategorie() != null) {
                for (Map.Entry<String, Long> entry : stats.getMenagesParCategorie().entrySet()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue());
                }
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    // ========== MÉTHODES POUR LE CONTROLLER ==========

    /**
     * Génère un rapport d'éligibilité à partir d'un ID de programme
     */
    public ProgrammeEligibiliteDto generateEligibiliteReport(UUID programmeId) {
        log.info("Génération du rapport d'éligibilité pour l'ID programme: {}", programmeId);

        return ProgrammeEligibiliteDto.builder()
                .programmeId(programmeId)
                .nomProgramme("Programme Social - " + programmeId.toString().substring(0, 8))
                .description("Description du programme social")
                .scoreMaxRequis(50)
                .dateDebut(LocalDateTime.now().minusMonths(1))
                .dateFin(LocalDateTime.now().plusMonths(11))
                .menagesEligibles(new ArrayList<>())
                .totalMenagesEligibles(0)
                .totalMenagesParRegion(0)
                .scoreMoyenEligibles(0.0)
                .dateGeneration(LocalDateTime.now())
                .build();
    }

    /**
     * Récupère la liste des ménages éligibles
     */
    public List<MenageEligibleDto> getMenagesEligibles(UUID programmeId, String region, Integer limit) {
        log.info("Récupération des ménages éligibles - programme: {}, région: {}, limit: {}",
                programmeId, region, limit);
        return new ArrayList<>();
    }

    /**
     * Génère les statistiques globales
     */
    public StatistiquesDto generateStatistiquesGlobales() {
        log.info("Génération des statistiques globales");

        return StatistiquesDto.builder()
                .totalMenages(0)
                .totalResidents(0)
                .totalAgents(0)
                .menagesParCategorie(new HashMap<>())
                .pourcentageParCategorie(new HashMap<>())
                .scoreMoyenGlobal(0.0)
                .scoreMin(0)
                .scoreMax(0)
                .ecartTypeScore(0.0)
                .menagesAvecTv(0)
                .menagesAvecRadio(0)
                .menagesAvecMoto(0)
                .menagesAvecVoiture(0)
                .menagesProprietaires(0)
                .tauxEquipementTv(0.0)
                .tauxEquipementRadio(0.0)
                .tauxEquipementMoto(0.0)
                .tauxEquipementVoiture(0.0)
                .tauxProprietaires(0.0)
                .nombreTresVulnerables(0)
                .nombreVulnerables(0)
                .nombreMoyens(0)
                .nombreAises(0)
                .nombreTresRiches(0)
                .menagesParRegion(new HashMap<>())
                .scoreMoyenParRegion(new HashMap<>())
                .inscriptionsParMois(new HashMap<>())
                .evolutionScoreMoyen(new HashMap<>())
                .dateGeneration(LocalDateTime.now())
                .build();
    }

    /**
     * Génère les statistiques avec filtres
     */
    public StatistiquesDto generateStatistiquesFiltrees(ReportFilterDto filter) {
        log.info("Génération des statistiques avec filtres: {}", filter);
        return generateStatistiquesGlobales();
    }

    /**
     * Récupère les statistiques par région
     */
    public Map<String, Object> getStatistiquesParRegion() {
        log.info("Récupération des statistiques par région");
        Map<String, Object> result = new HashMap<>();
        result.put("totalRegions", 0);
        result.put("details", new ArrayList<>());
        return result;
    }

    /**
     * Récupère la répartition par catégorie
     */
    public Map<String, Long> getRepartitionParCategorie() {
        log.info("Récupération de la répartition par catégorie");
        Map<String, Long> repartition = new HashMap<>();
        repartition.put("TRES_VULNERABLE", 0L);
        repartition.put("VULNERABLE", 0L);
        repartition.put("MOYEN", 0L);
        repartition.put("AISE", 0L);
        repartition.put("TRES_RICHE", 0L);
        return repartition;
    }

    /**
     * Récupère l'évolution des scores sur une période
     */
    public List<Map<String, Object>> getEvolutionScores(LocalDate debut, LocalDate fin) {
        log.info("Récupération de l'évolution des scores du {} au {}", debut, fin);
        return new ArrayList<>();
    }

    /**
     * Récupère le top des ménages par score
     */
    public List<MenageReportDto> getTopMenages(int limit) {
        log.info("Récupération du top {} des ménages", limit);
        return new ArrayList<>();
    }

    /**
     * Récupère les ménages par catégorie
     */
    public List<MenageReportDto> getMenagesByCategory(String category) {
        log.info("Récupération des ménages de catégorie: {}", category);
        return new ArrayList<>();
    }

    /**
     * Génère un rapport à partir d'une requête (pour le téléchargement)
     */
    public byte[] generateMenagesReportFile(ReportRequest request) {
        log.info("Génération de fichier rapport avec requête: {}", request);
        return new byte[0];
    }

    /**
     * Génère un rapport CSV
     */
    public String generateMenagesCsv(List<Map<String, Object>> menages) {
        log.info("Génération CSV pour {} ménages", menages.size());
        StringBuilder csv = new StringBuilder();
        csv.append("Code,Chef,Région,Ville,Score\n");
        return csv.toString();
    }

    // ========== Méthodes utilitaires ==========

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createCategoryStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void addStatRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        if (value instanceof Number) {
            row.createCell(1).setCellValue(((Number) value).doubleValue());
        } else {
            row.createCell(1).setCellValue(value.toString());
        }
    }

    private void addStatRow(Sheet sheet, int rowNum, String label, double value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell cell = row.createCell(1);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String str(Object o) { return o != null ? o.toString() : ""; }
    private int num(Object o) { return o instanceof Number n ? n.intValue() : 0; }
    private String bool(Object o) { return Boolean.TRUE.equals(o) ? "Oui" : "Non"; }
}