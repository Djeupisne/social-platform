package com.social.auth.entity;

import com.social.auth.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @NotAudited
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private String numeroCni;

    // ===== GESTION DU COMPTE =====
    @Builder.Default
    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Builder.Default
    @Column(name = "account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    @Builder.Default
    @Column(name = "credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    // ===== 2FA =====
    @Builder.Default
    @Column(name = "two_factor_enabled", nullable = false)
    private boolean twoFactorEnabled = false;

    @NotAudited
    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @Builder.Default
    @Column(name = "two_factor_verified", nullable = false)
    private boolean twoFactorVerified = false;

    // ===== SÉCURITÉ =====
    @Builder.Default
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    // ===== RELATIONS =====
    @Column(name = "menage_id")
    private UUID menageId; // Pour les chefs de ménage

    // ===== TIMESTAMPS =====
    @CreationTimestamp
    @NotAudited
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Incrémenter le compteur de tentatives échouées
     */
    public void incrementFailedAttempts() {
        this.failedAttempts++;
        if (this.failedAttempts >= 5) {
            this.accountNonLocked = false;
            this.lockTime = LocalDateTime.now();
        }
    }

    /**
     * Réinitialiser le compteur après une connexion réussie
     */
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.accountNonLocked = true;
        this.lockTime = null;
    }

    /**
     * Vérifier si le compte est verrouillé
     */
    public boolean isLocked() {
        return !this.accountNonLocked;
    }

    /**
     * Vérifier si le 2FA est configuré et vérifié
     */
    public boolean isTwoFactorFullyEnabled() {
        return this.twoFactorEnabled && this.twoFactorVerified;
    }

    /**
     * Activer le 2FA
     */
    public void enableTwoFactor(String secret) {
        this.twoFactorSecret = secret;
        this.twoFactorEnabled = true;
    }

    /**
     * Confirmer le 2FA après vérification
     */
    public void confirmTwoFactor() {
        this.twoFactorVerified = true;
    }

    /**
     * Désactiver le 2FA
     */
    public void disableTwoFactor() {
        this.twoFactorSecret = null;
        this.twoFactorEnabled = false;
        this.twoFactorVerified = false;
    }

    /**
     * Mettre à jour le mot de passe
     */
    public void updatePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
        this.lastPasswordChange = LocalDateTime.now();
    }

    /**
     * Vérifier si le mot de passe doit être changé (90 jours)
     */
    public boolean isPasswordExpired() {
        if (this.lastPasswordChange == null) {
            return true;
        }
        return this.lastPasswordChange.plusDays(90).isBefore(LocalDateTime.now());
    }

    /**
     * Déverrouiller le compte manuellement
     */
    public void unlock() {
        this.accountNonLocked = true;
        this.failedAttempts = 0;
        this.lockTime = null;
    }

    // ===== BONNES PRATIQUES =====

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.failedAttempts == 0) {
            this.failedAttempts = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                ", twoFactorEnabled=" + twoFactorEnabled +
                ", twoFactorVerified=" + twoFactorVerified +
                ", accountNonLocked=" + accountNonLocked +
                ", failedAttempts=" + failedAttempts +
                ", createdAt=" + createdAt +
                '}';
    }
}