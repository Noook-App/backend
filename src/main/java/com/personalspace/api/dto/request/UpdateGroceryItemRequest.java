package com.personalspace.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateGroceryItemRequest(
        @NotBlank(message = "Item name is required")
        @Size(max = 255, message = "Item name must not exceed 255 characters")
        String name,

        String quantity,

        List<UUID> labelIds
) {}
