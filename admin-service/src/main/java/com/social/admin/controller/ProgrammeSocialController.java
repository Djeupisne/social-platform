package com.social.admin.controller;

import com.social.admin.dto.request.ProgrammeSocialRequest;
import com.social.admin.dto.response.MenageEligibleResponse;
import com.social.admin.dto.response.ProgrammeSocialResponse;
import com.social.admin.service.ProgrammeSocialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/programmes")
@RequiredArgsConstructor
public class ProgrammeSocialController {

    private final ProgrammeSocialService programmeService;

    @PostMapping
    public ResponseEntity<ProgrammeSocialResponse> createProgramme(
            @Valid @RequestBody ProgrammeSocialRequest request,
            @RequestHeader("X-User-Id") String agentId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(programmeService.createProgramme(request, UUID.fromString(agentId)));
    }

    @GetMapping
    public ResponseEntity<List<ProgrammeSocialResponse>> getAllProgrammes() {
        return ResponseEntity.ok(programmeService.getAllProgrammes());
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<ProgrammeSocialResponse>> getProgrammesActifs() {
        return ResponseEntity.ok(programmeService.getProgrammesActifs());
    }

    @GetMapping("/{id}/menages-eligibles")
    public ResponseEntity<List<MenageEligibleResponse>> getMenagesEligibles(
            @PathVariable UUID id) {
        return ResponseEntity.ok(programmeService.getMenagesEligibles(id));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ProgrammeSocialResponse> toggleProgramme(@PathVariable UUID id) {
        return ResponseEntity.ok(programmeService.toggleProgramme(id));
    }
}