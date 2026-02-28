package com.social.admin.feign;

import com.social.admin.dto.response.MenageEligibleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "main-service", path = "/api/v1")
public interface MainServiceClient {

    @GetMapping("/menages/categorie/TRES_VULNERABLE")
    List<MenageEligibleResponse> getTresVulnerables();

    @GetMapping("/menages/categorie/VULNERABLE")
    List<MenageEligibleResponse> getVulnerables();

    @GetMapping("/menages/score-max")
    List<MenageEligibleResponse> getMenagesByScoreMax(@RequestParam int maxScore);
}