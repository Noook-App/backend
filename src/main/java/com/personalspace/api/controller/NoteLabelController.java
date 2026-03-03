package com.personalspace.api.controller;

import com.personalspace.api.dto.request.CreateNoteLabelRequest;
import com.personalspace.api.dto.request.UpdateNoteLabelRequest;
import com.personalspace.api.dto.response.NoteLabelResponse;
import com.personalspace.api.service.NoteLabelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes/labels")
public class NoteLabelController {

    private final NoteLabelService noteLabelService;

    public NoteLabelController(NoteLabelService noteLabelService) {
        this.noteLabelService = noteLabelService;
    }

    @PostMapping
    public ResponseEntity<NoteLabelResponse> createLabel(
            Principal principal,
            @Valid @RequestBody CreateNoteLabelRequest request) {
        NoteLabelResponse response = noteLabelService.createLabel(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<NoteLabelResponse>> getLabels(Principal principal) {
        List<NoteLabelResponse> response = noteLabelService.getLabels(principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteLabelResponse> updateLabel(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNoteLabelRequest request) {
        NoteLabelResponse response = noteLabelService.updateLabel(principal.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabel(
            Principal principal,
            @PathVariable UUID id) {
        noteLabelService.deleteLabel(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
