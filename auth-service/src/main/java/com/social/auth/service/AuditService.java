package com.social.auth.service;

import com.social.auth.entity.User;
import com.social.auth.entity.CustomRevisionEntity;
import com.social.auth.dto.UserAuditDto;
import com.social.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<UserAuditDto> getUserHistory(UUID userId) {
        Revisions<Long, User> revisions = userRepository.findRevisions(userId);

        return revisions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserAuditDto getUserRevisionDetails(UUID userId, Long revisionNumber) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        // Récupérer l'entité à la révision spécifique
        User user = auditReader.find(User.class, userId, revisionNumber);

        if (user == null) {
            return null;
        }

        // Récupérer le type de révision (ADD, MOD, DEL)
        RevisionType revisionType = getRevisionType(userId, revisionNumber);

        // Récupérer les métadonnées de révision
        CustomRevisionEntity revEntity = auditReader.findRevision(
                CustomRevisionEntity.class,
                revisionNumber
        );

        return convertToDetailedDto(user, revEntity, revisionType);
    }

    private RevisionType getRevisionType(UUID userId, Long revisionNumber) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        // Version plus simple pour récupérer le type de révision
        List<Object[]> results = auditReader.createQuery()
                .forRevisionsOfEntity(User.class, false, true)
                .add(AuditEntity.id().eq(userId))
                .add(AuditEntity.revisionNumber().eq(revisionNumber))
                .getResultList();

        if (!results.isEmpty()) {
            return (RevisionType) results.get(0)[1];
        }
        return null;
    }

    private UserAuditDto convertToDto(Revision<Long, User> revision) {
        UserAuditDto.UserAuditDtoBuilder builder = UserAuditDto.builder()
                .user(revision.getEntity())
                .revisionNumber(revision.getRequiredRevisionNumber())
                .revisionDate(revision.getRequiredRevisionInstant())
                .revisionType(revision.getMetadata().getRevisionType().toString());

        // Récupérer les informations personnalisées
        if (revision.getMetadata().getDelegate() instanceof CustomRevisionEntity) {
            CustomRevisionEntity revEntity = (CustomRevisionEntity) revision.getMetadata().getDelegate();
            builder.username(revEntity.getUsername())
                    .userEmail(revEntity.getUserEmail())
                    .action(revEntity.getAction())
                    .ipAddress(revEntity.getIpAddress());
        }

        return builder.build();
    }

    private UserAuditDto convertToDetailedDto(User user, CustomRevisionEntity revEntity, RevisionType revisionType) {
        UserAuditDto.UserAuditDtoBuilder builder = UserAuditDto.builder()
                .user(user)
                .revisionType(revisionType != null ? revisionType.toString() : "UNKNOWN");

        if (revEntity != null) {
            builder.revisionNumber(revEntity.getId())
                    .revisionDate(revEntity.getTimestamp().toInstant())
                    .username(revEntity.getUsername())
                    .userEmail(revEntity.getUserEmail())
                    .action(revEntity.getAction())
                    .ipAddress(revEntity.getIpAddress());
        }

        return builder.build();
    }
}