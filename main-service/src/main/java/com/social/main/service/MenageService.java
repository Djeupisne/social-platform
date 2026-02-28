package com.social.main.service;

import com.social.main.config.RabbitMQConfig;
import com.social.main.dto.request.MenageRequest;
import com.social.main.dto.request.ResidentRequest;
import com.social.main.dto.response.MenageDetailResponse;
import com.social.main.dto.response.MenageResponse;
import com.social.main.entity.Menage;
import com.social.main.entity.Resident;
import com.social.main.enums.CategorieSociale;
import com.social.main.enums.TrancheSalariale;
import com.social.main.event.MenageCreatedEvent;
import com.social.main.exception.ResourceNotFoundException;
import com.social.main.mapper.MenageMapper;
import com.social.main.mapper.ResidentMapper;
import com.social.main.repository.MenageRepository;
import com.social.main.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MenageService {

    private final MenageRepository menageRepository;
    private final ResidentRepository residentRepository;
    private final MenageMapper menageMapper;
    private final ResidentMapper residentMapper;
    private final RabbitTemplate rabbitTemplate;

    private static final AtomicInteger counter = new AtomicInteger(0);

    public MenageDetailResponse createMenage(MenageRequest request, UUID agentId) {
        // Vérifier qu'il y a exactement un chef
        long nbChefs = request.getResidents().stream()
                .filter(ResidentRequest::isChef).count();
        if (nbChefs != 1) {
            throw new IllegalArgumentException("Le ménage doit avoir exactement un chef");
        }

        // Vérifier unicité des CNI
        request.getResidents().forEach(r -> {
            if (residentRepository.existsByNumeroCni(r.getNumeroCni())) {
                throw new IllegalArgumentException(
                        "Le CNI " + r.getNumeroCni() + " est déjà enregistré");
            }
        });

        Menage menage = Menage.builder()
                .code(generateCode())
                .aTelevision(request.isATelevision())
                .aRadio(request.isARadio())
                .aMoto(request.isAMoto())
                .aVoiture(request.isAVoiture())
                .statutHabitation(request.getStatutHabitation())
                .region(request.getRegion())
                .ville(request.getVille())
                .quartier(request.getQuartier())
                .adresse(request.getAdresse())
                .agentId(agentId)
                .build();

        // Créer les résidents
        List<Resident> residents = request.getResidents().stream().map(residentRequest -> {
            Resident resident = residentMapper.toEntity(residentRequest);
            resident.setMenage(menage);
            return resident;
        }).collect(Collectors.toList());

        menage.setResidents(residents);

        // Calculer le score
        int score = calculateScore(menage);
        menage.setScore(score);
        menage.setCategorie(CategorieSociale.fromScore(score));

        Menage saved = menageRepository.save(menage);
        log.info("Ménage créé: {} avec score {} ({})", saved.getCode(), score, saved.getCategorie());

        // Publier l'événement
        MenageCreatedEvent event = MenageCreatedEvent.builder()
                .menageId(saved.getId())
                .code(saved.getCode())
                .score(saved.getScore())
                .categorie(saved.getCategorie())
                .region(saved.getRegion())
                .build();
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MENAGE_EXCHANGE,
                RabbitMQConfig.MENAGE_CREATED_KEY,
                event
        );

        return menageMapper.toDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<MenageResponse> getAllMenages(Pageable pageable) {
        return menageRepository.findAll(pageable).map(menageMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MenageDetailResponse getMenageById(UUID id) {
        Menage menage = menageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ménage introuvable: " + id));
        return menageMapper.toDetailResponse(menage);
    }

    @Transactional(readOnly = true)
    public MenageDetailResponse getMenageByCode(String code) {
        Menage menage = menageRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Ménage introuvable: " + code));
        return menageMapper.toDetailResponse(menage);
    }

    public MenageDetailResponse updateMenage(UUID id, MenageRequest request, UUID agentId) {
        Menage menage = menageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ménage introuvable: " + id));

        menage.setATelevision(request.isATelevision());
        menage.setARadio(request.isARadio());
        menage.setAMoto(request.isAMoto());
        menage.setAVoiture(request.isAVoiture());
        menage.setStatutHabitation(request.getStatutHabitation());
        menage.setRegion(request.getRegion());
        menage.setVille(request.getVille());
        menage.setQuartier(request.getQuartier());
        menage.setAdresse(request.getAdresse());

        // Recalculer le score
        int score = calculateScore(menage);
        menage.setScore(score);
        menage.setCategorie(CategorieSociale.fromScore(score));

        return menageMapper.toDetailResponse(menageRepository.save(menage));
    }

    @Transactional(readOnly = true)
    public List<MenageResponse> getMenagesByCategorie(CategorieSociale categorie) {
        return menageRepository.findByCategorie(categorie).stream()
                .map(menageMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Algorithme de calcul du score social
     */
    public int calculateScore(Menage menage) {
        int score = 0;

        // Télévision ET radio: 5 points (les deux ensemble)
        if (menage.isATelevision() && menage.isARadio()) {
            score += 5;
        }

        // Moto: 5 points
        if (menage.isAMoto()) {
            score += 5;
        }

        // Voiture: 10 points
        if (menage.isAVoiture()) {
            score += 10;
        }

        // Propriétaire: 20 points
        if (menage.getStatutHabitation() != null &&
                menage.getStatutHabitation() == com.social.main.enums.StatutHabitation.PROPRIETAIRE) {
            score += 20;
        }

        // Tranche salariale - on prend le score MAX parmi les résidents
        int maxSalaryPoints = 0;
        for (Resident resident : menage.getResidents()) {
            if (resident.getTrancheSalariale() != null) {
                int points = resident.getTrancheSalariale().getPoints();
                if (points > maxSalaryPoints) {
                    maxSalaryPoints = points;
                }
            }
        }
        score += maxSalaryPoints;

        return score;
    }

    private String generateCode() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        int num = counter.incrementAndGet();
        return String.format("MEN-%s-%05d", year, num);
    }
}