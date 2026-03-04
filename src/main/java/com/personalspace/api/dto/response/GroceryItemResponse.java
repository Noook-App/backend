package com.personalspace.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GroceryItemResponse(
        UUID id,
        String name,
        String quantity,
        boolean checked,
        List<GroceryLabelResponse> labels,
        Instant createdAt,
        Instant updatedAt
) {}
