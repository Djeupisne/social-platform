package com.social.main.repository;

import com.social.main.entity.Menage;
import com.social.main.enums.CategorieSociale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenageRepository extends JpaRepository<Menage, UUID>,
        RevisionRepository<Menage, UUID, Long> {

    Optional<Menage> findByCode(String code);

    Page<Menage> findByRegionContainingIgnoreCase(String region, Pageable pageable);

    List<Menage> findByCategorie(CategorieSociale categorie);

    List<Menage> findByCategorieIn(List<CategorieSociale> categories);

    @Query("SELECT m FROM Menage m WHERE m.score <= :maxScore")
    List<Menage> findMenagesWithScoreLessThanOrEqual(int maxScore);

    @Query("SELECT COUNT(m) FROM Menage m WHERE m.categorie = :categorie")
    long countByCategorie(CategorieSociale categorie);

    boolean existsByCode(String code);
}