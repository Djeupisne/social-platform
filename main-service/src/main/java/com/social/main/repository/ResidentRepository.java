package com.social.main.repository;

import com.social.main.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, UUID>,
        RevisionRepository<Resident, UUID, Long> {

    Optional<Resident> findByNumeroCni(String numeroCni);

    List<Resident> findByMenageId(UUID menageId);

    Optional<Resident> findByMenageIdAndChefTrue(UUID menageId);

    boolean existsByNumeroCni(String numeroCni);
}