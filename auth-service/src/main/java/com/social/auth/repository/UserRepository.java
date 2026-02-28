package com.social.auth.repository;

import com.social.auth.entity.User;
import com.social.auth.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends
        JpaRepository<User, UUID>,
        RevisionRepository<User, UUID, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByNumeroCni(String numeroCni);
    boolean existsByEmail(String email);
    boolean existsByNumeroCni(String numeroCni);

    // ── Login Chef de Ménage (Flutter Mobile) ──────────────────────────────
    // Recherche par nom complet + CNI + rôle CHEF_MENAGE
    // Utilisé par POST /api/v1/auth/chef/login
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(TRIM(u.fullName)) = LOWER(TRIM(:fullName)) AND " +
           "UPPER(TRIM(u.numeroCni)) = UPPER(TRIM(:numeroCni)) AND " +
           "u.role = :role")
    Optional<User> findChefByNomAndCni(
            @Param("fullName")  String fullName,
            @Param("numeroCni") String numeroCni,
            @Param("role")      UserRole role
    );
}
