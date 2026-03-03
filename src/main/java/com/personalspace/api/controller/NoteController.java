package com.personalspace.api.controller;

import com.personalspace.api.dto.request.CreateNoteRequest;
import com.personalspace.api.dto.request.UpdateNoteRequest;
import com.personalspace.api.dto.response.NoteResponse;
import com.personalspace.api.dto.response.PaginatedResponse;
import com.personalspace.api.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(
            Principal principal,
            @Valid @RequestBody CreateNoteRequest request) {
        NoteResponse response = noteService.createNote(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<NoteResponse>> getNotes(
            Principal principal,
            @RequestParam(defaultValue = "false") boolean archived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponse<NoteResponse> response = noteService.getNotes(principal.getName(), archived, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNote(
            Principal principal,
            @PathVariable UUID id) {
        NoteResponse response = noteService.getNote(principal.getName(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNoteRequest request) {
        NoteResponse response = noteService.updateNote(principal.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(
            Principal principal,
            @PathVariable UUID id) {
        noteService.deleteNote(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<NoteResponse>> searchNotes(
            Principal principal,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "false") boolean archived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponse<NoteResponse> response = noteService.searchNotes(principal.getName(), q, archived, page, size);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/pin")
    public ResponseEntity<NoteResponse> togglePin(
            Principal principal,
            @PathVariable UUID id) {
        NoteResponse response = noteService.togglePin(principal.getName(), id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<NoteResponse> toggleArchive(
            Principal principal,
            @PathVariable UUID id) {
        NoteResponse response = noteService.toggleArchive(principal.getName(), id);
        return ResponseEntity.ok(response);
    }
}
