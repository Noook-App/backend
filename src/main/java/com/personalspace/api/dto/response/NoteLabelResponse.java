package com.personalspace.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NoteLabelResponse(
        UUID id,
        String name,
        Instant createdAt
) {}
