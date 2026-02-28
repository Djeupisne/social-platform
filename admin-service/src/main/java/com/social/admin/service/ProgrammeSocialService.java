package com.social.admin.service;

import com.social.admin.dto.request.ProgrammeSocialRequest;
import com.social.admin.dto.response.MenageEligibleResponse;
import com.social.admin.dto.response.ProgrammeSocialResponse;
import com.social.admin.entity.ProgrammeSocial;
import com.social.admin.feign.MainServiceClient;
import com.social.admin.repository.ProgrammeSocialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProgrammeSocialService {

    private final ProgrammeSocialRepository programmeRepository;
    private final MainServiceClient mainServiceClient;

    public ProgrammeSocialResponse createProgramme(ProgrammeSocialRequest request, UUID agentId) {
        ProgrammeSocial programme = ProgrammeSocial.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .scoreMinEligibilite(request.getScoreMinEligibilite())
                .scoreMaxEligibilite(request.getScoreMaxEligibilite())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .budgetAlloue(request.getBudgetAlloue())
                .responsable(request.getResponsable())
                .region(request.getRegion())
                .creePar(agentId)
                .actif(true)
                .build();

        programme = programmeRepository.save(programme);
        return toResponse(programme);
    }

    @Transactional(readOnly = true)
    public List<ProgrammeSocialResponse> getAllProgrammes() {
        return programmeRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProgrammeSocialResponse> getProgrammesActifs() {
        return programmeRepository.findProgrammesActifs(LocalDate.now()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenageEligibleResponse> getMenagesEligibles(UUID programmeId) {
        ProgrammeSocial programme = programmeRepository.findById(programmeId)
                .orElseThrow(() -> new RuntimeException("Programme introuvable"));

        // Appel Feign vers main-service pour récupérer les ménages éligibles
        try {
            List<MenageEligibleResponse> menages = mainServiceClient
                    .getMenagesByScoreMax(programme.getScoreMaxEligibilite());
            // Filtrer par score minimum
            return menages.stream()
                    .filter(m -> m.getScore() >= programme.getScoreMinEligibilite())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erreur appel main-service: {}", e.getMessage());
            return List.of();
        }
    }

    public ProgrammeSocialResponse toggleProgramme(UUID id) {
        ProgrammeSocial programme = programmeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Programme introuvable"));
        programme.setActif(!programme.isActif());
        return toResponse(programmeRepository.save(programme));
    }

    private ProgrammeSocialResponse toResponse(ProgrammeSocial p) {
        ProgrammeSocialResponse response = new ProgrammeSocialResponse();
        response.setId(p.getId());
        response.setNom(p.getNom());
        response.setDescription(p.getDescription());
        response.setScoreMinEligibilite(p.getScoreMinEligibilite());
        response.setScoreMaxEligibilite(p.getScoreMaxEligibilite());
        response.setDateDebut(p.getDateDebut());
        response.setDateFin(p.getDateFin());
        response.setActif(p.isActif());
        response.setBudgetAlloue(p.getBudgetAlloue());
        response.setResponsable(p.getResponsable());
        response.setRegion(p.getRegion());
        response.setCreatedAt(p.getCreatedAt());
        return response;
    }
}