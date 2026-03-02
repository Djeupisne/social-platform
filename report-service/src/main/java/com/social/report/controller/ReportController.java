package com.social.report.controller;
import java.util.stream.Collectors;
import com.social.report.dto.*;
import com.social.report.enums.FormatRapport;
import com.social.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class ReportController {

    private final ReportService reportService;

    /**
     * Export des ménages au format Excel - Version compatible avec le service
     */
    @PostMapping(value = "/menages/excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportMenagesExcel(
            @RequestBody List<Map<String, Object>> menages) throws Exception {

        log.info("Export Excel de {} ménages", menages.size());

        byte[] report = reportService.generateMenagesReport(convertMaps(menages));

        String filename = "menages_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(report);
    }
    private List<MenageReportDto> convertMaps(List<Map<String, Object>> menages) {
        return menages.stream().map(m -> MenageReportDto.builder()
                .code(str(m.get("code")))
                .chefNom(str(m.get("chefNom")))
                .region(str(m.get("region")))
                .ville(str(m.get("ville")))
                .quartier(str(m.get("quartier")))
                .score(num(m.get("score")))
                .categorie(str(m.get("categorie")))
                .categorieLabel(str(m.get("categorieLabel")))
                .hasTv(Boolean.TRUE.equals(m.get("hasTv")))
                .hasRadio(Boolean.TRUE.equals(m.get("hasRadio")))
                .hasMotorcycle(Boolean.TRUE.equals(m.get("hasMotorcycle")))
                .hasCar(Boolean.TRUE.equals(m.get("hasCar")))
                .isOwner(Boolean.TRUE.equals(m.get("isOwner")))
                .nombreResidents(num(m.get("nombreResidents")))
                .maxSalary(m.get("maxSalary") instanceof Number n ? n.doubleValue() : 0.0)
                .build()
        ).collect(Collectors.toList());
    }

    private String str(Object o) { return o != null ? o.toString() : ""; }
    private int num(Object o) { return o instanceof Number n ? n.intValue() : 0; }
    /**
     * Génération de rapport avec filtres
     */
    @PostMapping(value = "/menages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportResponse> generateMenagesReport(
            @RequestBody ReportRequest request) {

        log.info("Génération de rapport avec filtres: {}", request);

        if (!request.isValidFormat()) {
            return ResponseEntity.badRequest().body(
                    ReportResponse.builder()
                            .success(false)
                            .message("Format non supporté. Utilisez EXCEL, CSV ou PDF")
                            .generatedAt(LocalDateTime.now())
                            .build()
            );
        }

        // Conversion de ReportRequest vers une liste de MenageReportDto (simulé pour l'instant)
        List<MenageReportDto> menages = List.of(); // À remplacer par la vraie logique

        ReportResponse response = ReportResponse.builder()
                .success(true)
                .message("Rapport généré avec succès")
                .format(request.getFormat())
                .generatedAt(LocalDateTime.now())
                .totalRecords(0)
                .filtreRegion(request.getRegion())
                .filtreCategorie(request.getCategorie())
                .periodeDebut(request.getDateDebut() != null ? request.getDateDebut().atStartOfDay() : null)
                .periodeFin(request.getDateFin() != null ? request.getDateFin().atStartOfDay() : null)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Téléchargement du rapport généré
     */
    @PostMapping(value = "/menages/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<ByteArrayResource> downloadMenagesReport(
            @RequestBody ReportRequest request) {

        log.info("Téléchargement de rapport: format {}", request.getFormat());

        byte[] data = reportService.generateMenagesReportFile(request);
        FormatRapport format = FormatRapport.valueOf(request.getFormat().toUpperCase());

        String filename = "rapport_menages_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                format.getExtension();

        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(format.getMimeType()))
                .contentLength(data.length)
                .body(resource);
    }

    /**
     * Rapport d'éligibilité pour un programme social
     */
    @GetMapping("/eligibilite/programme/{programmeId}")
    public ResponseEntity<ProgrammeEligibiliteDto> getEligibiliteProgramme(
            @PathVariable UUID programmeId) {

        log.info("Rapport d'éligibilité pour le programme: {}", programmeId);

        ProgrammeEligibiliteDto report = reportService.generateEligibiliteReport(programmeId);
        return ResponseEntity.ok(report);
    }

    /**
     * Liste des ménages éligibles à un programme
     */
    @GetMapping("/eligibilite/programme/{programmeId}/menages")
    public ResponseEntity<List<MenageEligibleDto>> getMenagesEligibles(
            @PathVariable UUID programmeId,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer limit) {

        log.info("Liste des ménages éligibles pour programme: {}", programmeId);

        List<MenageEligibleDto> menages = reportService.getMenagesEligibles(programmeId, region, limit);
        return ResponseEntity.ok(menages);
    }

    /**
     * Statistiques globales
     */
    @GetMapping("/statistiques")
    public ResponseEntity<StatistiquesDto> getStatistiquesGlobales() {

        log.info("Génération des statistiques globales");

        StatistiquesDto stats = reportService.generateStatistiquesGlobales();
        return ResponseEntity.ok(stats);
    }

    /**
     * Statistiques avec filtres avancés
     */
    @PostMapping("/statistiques/filtre")
    public ResponseEntity<StatistiquesDto> getStatistiquesFiltrees(
            @RequestBody ReportFilterDto filter) {

        log.info("Génération des statistiques avec filtres");

        StatistiquesDto stats = reportService.generateStatistiquesFiltrees(filter);
        return ResponseEntity.ok(stats);
    }

    /**
     * Statistiques par région
     */
    @GetMapping("/statistiques/regions")
    public ResponseEntity<Map<String, Object>> getStatistiquesParRegion() {

        log.info("Génération des statistiques par région");

        Map<String, Object> statsParRegion = reportService.getStatistiquesParRegion();
        return ResponseEntity.ok(statsParRegion);
    }

    /**
     * Statistiques par catégorie sociale
     */
    @GetMapping("/statistiques/categories")
    public ResponseEntity<Map<String, Long>> getRepartitionParCategorie() {

        log.info("Génération de la répartition par catégorie");

        Map<String, Long> repartition = reportService.getRepartitionParCategorie();
        return ResponseEntity.ok(repartition);
    }

    /**
     * Évolution des scores sur une période
     */
    @GetMapping("/statistiques/evolution")
    public ResponseEntity<List<Map<String, Object>>> getEvolutionScores(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        log.info("Évolution des scores du {} au {}", debut, fin);

        List<Map<String, Object>> evolution = reportService.getEvolutionScores(debut, fin);
        return ResponseEntity.ok(evolution);
    }

    /**
     * Top des ménages par score
     */
    @GetMapping("/menages/top")
    public ResponseEntity<List<MenageReportDto>> getTopMenages(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Top {} ménages par score", limit);

        List<MenageReportDto> topMenages = reportService.getTopMenages(limit);
        return ResponseEntity.ok(topMenages);
    }

    /**
     * Ménages très vulnérables (score < 20)
     */
    @GetMapping("/menages/tres-vulnerables")
    public ResponseEntity<List<MenageReportDto>> getMenagesTresVulnerables() {

        log.info("Liste des ménages très vulnérables");

        List<MenageReportDto> menages = reportService.getMenagesByCategory("TRES_VULNERABLE");
        return ResponseEntity.ok(menages);
    }

    /**
     * Export CSV simple
     */
    @PostMapping(value = "/menages/csv", produces = "text/csv")
    public ResponseEntity<String> exportMenagesCsv(
            @RequestBody List<Map<String, Object>> menages) {

        log.info("Export CSV de {} ménages", menages.size());

        String csv = reportService.generateMenagesCsv(menages);

        String filename = "menages_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    /**
     * Health check du service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "report-service",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * Gestion des exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ReportResponse> handleException(Exception e) {
        log.error("Erreur dans le service de reporting", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ReportResponse.builder()
                        .success(false)
                        .message("Erreur lors de la génération du rapport: " + e.getMessage())
                        .generatedAt(LocalDateTime.now())
                        .build());
    }
}