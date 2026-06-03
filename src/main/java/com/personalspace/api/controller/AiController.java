package com.personalspace.api.controller;

import com.personalspace.api.dto.request.AiRecommendRequest;
import com.personalspace.api.dto.response.AiRecommendResponse;
import com.personalspace.api.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<AiRecommendResponse> recommend(
            @Valid @RequestBody AiRecommendRequest request) {
        AiRecommendResponse response = aiService.recommend(request);
        return ResponseEntity.ok(response);
    }
}
