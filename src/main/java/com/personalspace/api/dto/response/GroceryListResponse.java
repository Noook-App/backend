package com.personalspace.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GroceryListResponse(
        UUID id,
        String title,
        boolean archived,
        List<GroceryLabelResponse> labels,
        List<GroceryItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {}
