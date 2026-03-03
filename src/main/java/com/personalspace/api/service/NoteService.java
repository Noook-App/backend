package com.personalspace.api.service;

import com.personalspace.api.dto.request.CreateNoteRequest;
import com.personalspace.api.dto.request.UpdateNoteRequest;
import com.personalspace.api.dto.response.NoteLabelResponse;
import com.personalspace.api.dto.response.NoteResponse;
import com.personalspace.api.dto.response.PaginatedResponse;
import com.personalspace.api.exception.ResourceNotFoundException;
import com.personalspace.api.model.entity.NoteLabel;
import com.personalspace.api.model.entity.Note;
import com.personalspace.api.model.entity.User;
import com.personalspace.api.repository.NoteLabelRepository;
import com.personalspace.api.repository.NoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteLabelRepository noteLabelRepository;
    private final UserService userService;

    public NoteService(NoteRepository noteRepository, NoteLabelRepository noteLabelRepository, UserService userService) {
        this.noteRepository = noteRepository;
        this.noteLabelRepository = noteLabelRepository;
        this.userService = userService;
    }

    @Transactional
    public NoteResponse createNote(String email, CreateNoteRequest request) {
        User user = userService.getUserByEmail(email);

        Note note = new Note();
        note.setTitle(request.title());
        note.setContent(request.content());
        note.setPinned(request.pinned() != null && request.pinned());
        note.setUser(user);
        note.setLabels(resolveLabels(request.labelIds(), user));

        Note saved = noteRepository.save(note);
        return toNoteResponse(saved);
    }

    public NoteResponse getNote(String email, UUID noteId) {
        User user = userService.getUserByEmail(email);
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));
        return toNoteResponse(note);
    }

    public PaginatedResponse<NoteResponse> getNotes(String email, boolean archived, int page, int size) {
        User user = userService.getUserByEmail(email);
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("pinned"),
                Sort.Order.desc("createdAt")
        ));
        Page<Note> notePage = noteRepository.findByUserAndArchived(user, archived, pageable);
        return toPaginatedResponse(notePage);
    }

    @Transactional
    public NoteResponse updateNote(String email, UUID noteId, UpdateNoteRequest request) {
        User user = userService.getUserByEmail(email);
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));

        note.setTitle(request.title());
        note.setContent(request.content());
        if (request.pinned() != null) {
            note.setPinned(request.pinned());
        }
        if (request.archived() != null) {
            note.setArchived(request.archived());
        }
        note.setLabels(resolveLabels(request.labelIds(), user));

        Note saved = noteRepository.save(note);
        return toNoteResponse(saved);
    }

    @Transactional
    public void deleteNote(String email, UUID noteId) {
        User user = userService.getUserByEmail(email);
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));
        noteRepository.delete(note);
    }

    @Transactional
    public NoteResponse togglePin(String email, UUID noteId) {
        User user = userService.getUserByEmail(email);
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));
        note.setPinned(!note.isPinned());
        Note saved = noteRepository.save(note);
        return toNoteResponse(saved);
    }

    @Transactional
    public NoteResponse toggleArchive(String email, UUID noteId) {
        User user = userService.getUserByEmail(email);
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));
        note.setArchived(!note.isArchived());
        Note saved = noteRepository.save(note);
        return toNoteResponse(saved);
    }

    public PaginatedResponse<NoteResponse> searchNotes(String email, String query, boolean archived, int page, int size) {
        if (query == null || query.isBlank()) {
            return getNotes(email, archived, page, size);
        }

        User user = userService.getUserByEmail(email);
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("pinned"),
                Sort.Order.desc("createdAt")
        ));
        Page<Note> notePage = noteRepository.searchByUserAndQuery(user, query, archived, pageable);
        return toPaginatedResponse(notePage);
    }

    private Set<NoteLabel> resolveLabels(List<UUID> labelIds, User user) {
        if (labelIds == null || labelIds.isEmpty()) {
            return new HashSet<>();
        }
        return labelIds.stream()
                .map(id -> noteLabelRepository.findByIdAndUser(id, user)
                        .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + id)))
                .collect(Collectors.toSet());
    }

    private NoteResponse toNoteResponse(Note note) {
        List<NoteLabelResponse> labelResponses = note.getLabels().stream()
                .map(label -> new NoteLabelResponse(label.getId(), label.getName(), label.getCreatedAt()))
                .sorted(Comparator.comparing(NoteLabelResponse::name))
                .toList();

        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.isPinned(),
                note.isArchived(),
                labelResponses,
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    private PaginatedResponse<NoteResponse> toPaginatedResponse(Page<Note> page) {
        List<NoteResponse> content = page.getContent().stream()
                .map(this::toNoteResponse)
                .toList();
        return new PaginatedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
