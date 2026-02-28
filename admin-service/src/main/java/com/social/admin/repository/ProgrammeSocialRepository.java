package com.social.admin.repository;

import com.social.admin.entity.ProgrammeSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProgrammeSocialRepository extends JpaRepository<ProgrammeSocial, UUID> {
    List<ProgrammeSocial> findByActifTrue();
    List<ProgrammeSocial> findByRegionOrRegionIsNull(String region);

    @Query("SELECT p FROM ProgrammeSocial p WHERE p.actif = true " +
            "AND (p.dateFin IS NULL OR p.dateFin >= :today)")
    List<ProgrammeSocial> findProgrammesActifs(LocalDate today);
}