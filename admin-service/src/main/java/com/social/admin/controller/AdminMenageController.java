package com.social.admin.controller;

import com.social.admin.dto.response.StatistiquesResponse;
import com.social.admin.service.AdminMenageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminMenageController {

    private final AdminMenageService adminMenageService;

    @GetMapping("/statistiques")
    public ResponseEntity<StatistiquesResponse> getStatistiques() {
        return ResponseEntity.ok(adminMenageService.getStatistiques());
    }
}