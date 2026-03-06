package com.personalspace.api.dto.request;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record UpdateNoteRequest(
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        String content,

        Boolean pinned,

        Boolean archived,

        List<UUID> labelIds
) {}
