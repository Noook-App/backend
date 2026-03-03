package com.personalspace.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NoteResponse(
        UUID id,
        String title,
        String content,
        boolean pinned,
        boolean archived,
        List<NoteLabelResponse> labels,
        Instant createdAt,
        Instant updatedAt
) {}
