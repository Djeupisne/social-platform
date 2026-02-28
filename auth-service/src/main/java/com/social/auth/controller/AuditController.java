package com.social.auth.controller;

import com.social.auth.dto.UserAuditDto;
import com.social.auth.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<UserAuditDto>> getUserHistory(@PathVariable UUID userId) {
        List<UserAuditDto> history = auditService.getUserHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/users/{userId}/revision/{revisionNumber}")
    public ResponseEntity<UserAuditDto> getUserRevisionDetails(
            @PathVariable UUID userId,
            @PathVariable Long revisionNumber) {
        UserAuditDto details = auditService.getUserRevisionDetails(userId, revisionNumber);
        return details != null ? ResponseEntity.ok(details) : ResponseEntity.notFound().build();
    }
}