package com.personalspace.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AiRecommendRequest(
        @NotBlank String type,
        String context,
        String currentContent,
        String title
) {}
