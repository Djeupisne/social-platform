package com.social.main.entity;

import com.social.main.enums.CategorieSociale;
import com.social.main.enums.StatutHabitation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "menages")
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code; // code unique du ménage (ex: MEN-2025-001)

    // Infos sur les biens du ménage
    private boolean aTelevision;
    private boolean aRadio;
    private boolean aMoto;
    private boolean aVoiture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutHabitation statutHabitation;

    // Score calculé
    private int score;

    @Enumerated(EnumType.STRING)
    private CategorieSociale categorie;

    // Région/localisation
    private String region;
    private String ville;
    private String quartier;
    private String adresse;

    // Agent qui a enregistré le ménage
    private UUID agentId;

    @OneToMany(mappedBy = "menage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Resident> residents = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Méthode utilitaire pour récupérer le chef
    @Transient
    public Resident getChef() {
        return residents.stream()
                .filter(Resident::isChef)
                .findFirst()
                .orElse(null);
    }
}