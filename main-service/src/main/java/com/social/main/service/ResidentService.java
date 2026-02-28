package com.social.main.service;

import com.social.main.dto.request.ResidentRequest;
import com.social.main.dto.response.ResidentResponse;
import com.social.main.entity.Menage;
import com.social.main.entity.Resident;
import com.social.main.exception.ResourceNotFoundException;
import com.social.main.mapper.ResidentMapper;
import com.social.main.repository.MenageRepository;
import com.social.main.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final MenageRepository menageRepository;
    private final ResidentMapper residentMapper;
    private final MenageService menageService;

    // --- AJOUTEZ CETTE MÉTHODE ---
    @Transactional(readOnly = true)
    public List<ResidentResponse> getAllResidents() {
        return residentRepository.findAll()
                .stream()
                .map(residentMapper::toResponse)
                .collect(Collectors.toList());
    }
    // -----------------------------

    public ResidentResponse addResident(UUID menageId, ResidentRequest request) {
        Menage menage = menageRepository.findById(menageId)
                .orElseThrow(() -> new ResourceNotFoundException("Ménage introuvable"));

        if (residentRepository.existsByNumeroCni(request.getNumeroCni())) {
            throw new IllegalArgumentException("Ce CNI est déjà enregistré");
        }

        Resident resident = residentMapper.toEntity(request);
        resident.setMenage(menage);
        Resident saved = residentRepository.save(resident);

        // Recalculer le score du ménage
        int newScore = menageService.calculateScore(menage);
        menage.setScore(newScore);
        menage.setCategorie(com.social.main.enums.CategorieSociale.fromScore(newScore));
        menageRepository.save(menage);

        return residentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ResidentResponse> getResidentsByMenage(UUID menageId) {
        return residentRepository.findByMenageId(menageId).stream()
                .map(residentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResidentResponse getResidentById(UUID id) {
        return residentMapper.toResponse(
                residentRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Résident introuvable"))
        );
    }

    @Transactional(readOnly = true)
    public ResidentResponse getResidentByCni(String cni) {
        return residentMapper.toResponse(
                residentRepository.findByNumeroCni(cni)
                        .orElseThrow(() -> new ResourceNotFoundException("Résident introuvable avec CNI: " + cni))
        );
    }

    public ResidentResponse updateResident(UUID id, ResidentRequest request) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Résident introuvable"));

        resident.setNom(request.getNom());
        resident.setPrenom(request.getPrenom());
        resident.setNationalite(request.getNationalite());
        resident.setNiveauDiplome(request.getNiveauDiplome());
        resident.setTrancheSalariale(request.getTrancheSalariale());
        resident.setDateNaissance(request.getDateNaissance());
        resident.setTelephone(request.getTelephone());

        Resident saved = residentRepository.save(resident);

        // Recalculer le score
        Menage menage = resident.getMenage();
        int newScore = menageService.calculateScore(menage);
        menage.setScore(newScore);
        menage.setCategorie(com.social.main.enums.CategorieSociale.fromScore(newScore));
        menageRepository.save(menage);

        return residentMapper.toResponse(saved);
    }

    public void deleteResident(UUID id) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Résident introuvable"));
        if (resident.isChef()) {
            throw new IllegalStateException("Impossible de supprimer le chef du ménage");
        }
        residentRepository.delete(resident);
    }
}