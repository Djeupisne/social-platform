package com.social.report.util;
import com.social.report.dto.MenageEligibleDto;
import com.social.report.dto.ProgrammeEligibiliteDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ExcelGenerator {

    public byte[] generateMenagesReport(List<?> menages) throws IOException {
        log.info("Génération Excel pour {} ménages", menages.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ménages");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            String[] headers = {
                    "Code Ménage", "Chef de ménage", "Région", "Ville", "Quartier",
                    "Score", "Catégorie", "Télévision", "Radio", "Moto", "Voiture",
                    "Statut Habitation", "Propriétaire", "Nb Résidents", "Date création"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Object obj : menages) {
                if (obj instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) obj;
                    Row row = sheet.createRow(rowNum++);

                    row.createCell(0).setCellValue(str(m.get("code")));
                    row.createCell(1).setCellValue(str(m.get("chefNom")));
                    row.createCell(2).setCellValue(str(m.get("region")));
                    row.createCell(3).setCellValue(str(m.get("ville")));
                    row.createCell(4).setCellValue(str(m.get("quartier")));
                    row.createCell(5).setCellValue(num(m.get("score")));
                    row.createCell(6).setCellValue(str(m.get("categorieLabel")));
                    row.createCell(7).setCellValue(bool(m.get("hasTv")));
                    row.createCell(8).setCellValue(bool(m.get("hasRadio")));
                    row.createCell(9).setCellValue(bool(m.get("hasMotorcycle")));
                    row.createCell(10).setCellValue(bool(m.get("hasCar")));
                    row.createCell(11).setCellValue(str(m.get("statutHabitation")));
                    row.createCell(12).setCellValue(bool(m.get("owner")));
                    row.createCell(13).setCellValue(num(m.get("nombreResidents")));

                    Cell dateCell = row.createCell(14);
                    dateCell.setCellValue(formatDate(m.get("dateCreation")));
                    dateCell.setCellStyle(dateStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    public byte[] generateEligibiliteReport(ProgrammeEligibiliteDto programme) throws IOException {
        log.info("Génération Excel d'éligibilité pour programme: {}", programme.getNomProgramme());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Éligibilité");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // En-tête du programme
            Row progRow = sheet.createRow(0);
            progRow.createCell(0).setCellValue("Programme:");
            progRow.createCell(1).setCellValue(programme.getNomProgramme());

            Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("Date génération:");
            Cell dateGenCell = dateRow.createCell(1);
            dateGenCell.setCellValue(formatDate(programme.getDateGeneration()));
            dateGenCell.setCellStyle(dateStyle);

            Row totalRow = sheet.createRow(2);
            totalRow.createCell(0).setCellValue("Total éligibles:");
            totalRow.createCell(1).setCellValue(programme.getTotalMenagesEligibles());

            sheet.createRow(3); // ligne vide

            String[] headers = {
                    "Code Ménage", "Chef de ménage", "Contact", "Score",
                    "Catégorie", "Région", "Ville", "Nb Résidents", "Bénéficiaire actuel"
            };

            Row headerRow = sheet.createRow(4);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 5;
            if (programme.getMenagesEligibles() != null) {
                for (MenageEligibleDto m : programme.getMenagesEligibles()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(m.getCodeMenage());
                    row.createCell(1).setCellValue(m.getChefNom());
                    row.createCell(2).setCellValue(m.getChefContact());
                    row.createCell(3).setCellValue(m.getScore());
                    row.createCell(4).setCellValue(m.getCategorie());
                    row.createCell(5).setCellValue(m.getRegion());
                    row.createCell(6).setCellValue(m.getVille());
                    row.createCell(7).setCellValue(m.getNombreResidents());
                    row.createCell(8).setCellValue(m.isBeneficiaireActuel() ? "Oui" : "Non");
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

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

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }

    private String str(Object o) {
        return o != null ? o.toString() : "";
    }

    private int num(Object o) {
        if (o instanceof Number) return ((Number) o).intValue();
        return 0;
    }

    private String bool(Object o) {
        return Boolean.TRUE.equals(o) ? "Oui" : "Non";
    }

    private String formatDate(Object date) {
        if (date == null) return "";
        if (date instanceof LocalDateTime) {
            return ((LocalDateTime) date).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return date.toString();
    }
}