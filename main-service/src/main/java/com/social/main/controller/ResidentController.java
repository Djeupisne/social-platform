package com.social.main.controller;

import com.social.main.dto.request.ResidentRequest;
import com.social.main.dto.response.ResidentResponse;
import com.social.main.service.ResidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    @PostMapping("/menages/{menageId}/residents")
    public ResponseEntity<ResidentResponse> addResident(
            @PathVariable UUID menageId,
            @Valid @RequestBody ResidentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(residentService.addResident(menageId, request));
    }

    @GetMapping("/menages/{menageId}/residents")
    public ResponseEntity<List<ResidentResponse>> getResidentsByMenage(
            @PathVariable UUID menageId) {
        return ResponseEntity.ok(residentService.getResidentsByMenage(menageId));
    }
@GetMapping("/residents")
public ResponseEntity<List<ResidentResponse>> getAllResidents() {
    return ResponseEntity.ok(residentService.getAllResidents());
}
    @GetMapping("/residents/{id}")
    public ResponseEntity<ResidentResponse> getResidentById(@PathVariable UUID id) {
        return ResponseEntity.ok(residentService.getResidentById(id));
    }

    @GetMapping("/residents/cni/{cni}")
    public ResponseEntity<ResidentResponse> getResidentByCni(@PathVariable String cni) {
        return ResponseEntity.ok(residentService.getResidentByCni(cni));
    }

    @PutMapping("/residents/{id}")
    public ResponseEntity<ResidentResponse> updateResident(
            @PathVariable UUID id,
            @Valid @RequestBody ResidentRequest request) {
        return ResponseEntity.ok(residentService.updateResident(id, request));
    }

    @DeleteMapping("/residents/{id}")
    public ResponseEntity<Void> deleteResident(@PathVariable UUID id) {
        residentService.deleteResident(id);
        return ResponseEntity.noContent().build();
    }
}