package com.social.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "programmes_sociaux")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProgrammeSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Critères d'éligibilité par score
    private int scoreMaxEligibilite; // ménages avec score <= scoreMax sont éligibles
    private int scoreMinEligibilite; // 0 par défaut

    private LocalDate dateDebut;
    private LocalDate dateFin;

    private boolean actif = true;

    // Budget alloué
    private Long budgetAlloue;

    private String responsable;
    private String region; // null = national

    private UUID creePar; // agent qui a créé

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}