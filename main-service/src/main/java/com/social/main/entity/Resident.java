package com.social.main.entity;

import com.social.main.enums.NiveauDiplome;
import com.social.main.enums.TrancheSalariale;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "residents")
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String numeroCni;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    private String nationalite;

    @Enumerated(EnumType.STRING)
    private NiveauDiplome niveauDiplome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrancheSalariale trancheSalariale;

    private LocalDate dateNaissance;

    private String telephone;

    private boolean chef; // true si c'est le chef du ménage

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menage_id")
    private Menage menage;

    // Référence user account (optional - only for chefs who have accounts)
    private UUID userId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}