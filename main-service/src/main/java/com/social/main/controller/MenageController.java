package com.social.main.controller;

import com.social.main.dto.request.MenageRequest;
import com.social.main.dto.response.MenageDetailResponse;
import com.social.main.dto.response.MenageResponse;
import com.social.main.enums.CategorieSociale;
import com.social.main.service.MenageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menages")
@RequiredArgsConstructor
public class MenageController {

    private final MenageService menageService;

    @PostMapping
    public ResponseEntity<MenageDetailResponse> createMenage(
            @Valid @RequestBody MenageRequest request,
            @RequestHeader("X-User-Id") String agentId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menageService.createMenage(request, UUID.fromString(agentId)));
    }

    @GetMapping
    public ResponseEntity<Page<MenageResponse>> getAllMenages(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(menageService.getAllMenages(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenageDetailResponse> getMenageById(@PathVariable UUID id) {
        return ResponseEntity.ok(menageService.getMenageById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<MenageDetailResponse> getMenageByCode(@PathVariable String code) {
        return ResponseEntity.ok(menageService.getMenageByCode(code));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenageDetailResponse> updateMenage(
            @PathVariable UUID id,
            @Valid @RequestBody MenageRequest request,
            @RequestHeader("X-User-Id") String agentId) {
        return ResponseEntity.ok(
                menageService.updateMenage(id, request, UUID.fromString(agentId)));
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<MenageResponse>> getMenagesByCategorie(
            @PathVariable CategorieSociale categorie) {
        return ResponseEntity.ok(menageService.getMenagesByCategorie(categorie));
    }
}