package com.social.report.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "main-service", path = "/api/v1")
public interface MainServiceClient {

    @GetMapping("/menages")
    Map<String, Object> getAllMenages(@RequestParam int page, @RequestParam int size);
}