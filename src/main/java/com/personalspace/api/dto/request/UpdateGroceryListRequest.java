package com.personalspace.api.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateGroceryListRequest(
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        List<UUID> labelIds
) {}
