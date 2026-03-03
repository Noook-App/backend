package com.personalspace.api.service;

import com.personalspace.api.dto.request.CreateNoteRequest;
import com.personalspace.api.dto.request.UpdateNoteRequest;
import com.personalspace.api.dto.response.NoteResponse;
import com.personalspace.api.dto.response.PaginatedResponse;
import com.personalspace.api.exception.ResourceNotFoundException;
import com.personalspace.api.model.entity.NoteLabel;
import com.personalspace.api.model.entity.Note;
import com.personalspace.api.model.entity.User;
import com.personalspace.api.repository.NoteLabelRepository;
import com.personalspace.api.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteLabelRepository noteLabelRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NoteService noteService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
    }

    @Test
    void createNote_shouldReturnNoteResponse() {
        CreateNoteRequest request = new CreateNoteRequest("Test Note", "Content", null, null);

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        Note saved = createTestNote(UUID.randomUUID(), "Test Note", "Content", false, false, user);
        when(noteRepository.save(any(Note.class))).thenReturn(saved);

        NoteResponse response = noteService.createNote("test@test.com", request);

        assertNotNull(response);
        assertEquals("Test Note", response.title());
        assertEquals("Content", response.content());
        assertFalse(response.pinned());
    }

    @Test
    void createNote_shouldResolveLabels() {
        UUID labelId = UUID.randomUUID();
        CreateNoteRequest request = new CreateNoteRequest("Test Note", "Content", null, List.of(labelId));

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        NoteLabel label = new NoteLabel();
        label.setId(labelId);
        label.setName("Work");
        label.setUser(user);
        when(noteLabelRepository.findByIdAndUser(labelId, user)).thenReturn(Optional.of(label));

        Note saved = createTestNote(UUID.randomUUID(), "Test Note", "Content", false, false, user);
        saved.setLabels(Set.of(label));
        when(noteRepository.save(any(Note.class))).thenReturn(saved);

        NoteResponse response = noteService.createNote("test@test.com", request);

        assertNotNull(response);
        assertEquals(1, response.labels().size());
    }

    @Test
    void createNote_shouldThrowWhenLabelNotFound() {
        UUID labelId = UUID.randomUUID();
        CreateNoteRequest request = new CreateNoteRequest("Test Note", "Content", null, List.of(labelId));

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(noteLabelRepository.findByIdAndUser(labelId, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> noteService.createNote("test@test.com", request));
    }

    @Test
    void getNote_shouldReturnNoteResponse() {
        UUID noteId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        Note note = createTestNote(noteId, "Test Note", "Content", false, false, user);
        when(noteRepository.findByIdAndUser(noteId, user)).thenReturn(Optional.of(note));

        NoteResponse response = noteService.getNote("test@test.com", noteId);

        assertEquals("Test Note", response.title());
    }

    @Test
    void getNote_shouldThrowWhenNotFound() {
        UUID noteId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(noteRepository.findByIdAndUser(noteId, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> noteService.getNote("test@test.com", noteId));
    }

    @Test
    void getNotes_shouldReturnPaginatedResponse() {
        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        Note note = createTestNote(UUID.randomUUID(), "Test Note", "Content", false, false, user);
        Page<Note> page = new PageImpl<>(List.of(note));
        when(noteRepository.findByUserAndArchived(eq(user), eq(false), any(Pageable.class))).thenReturn(page);

        PaginatedResponse<NoteResponse> response = noteService.getNotes("test@test.com", false, 0, 10);

        assertEquals(1, response.content().size());
        assertEquals(1, response.totalElements());
    }

    @Test
    void updateNote_shouldUpdateFields() {
        UUID noteId = UUID.randomUUID();
        UpdateNoteRequest request = new UpdateNoteRequest("Updated Title", "Updated Content", true, false, null);

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        Note existing = createTestNote(noteId, "Old Title", "Old Content", false, false, user);
        when(noteRepository.findByIdAndUser(noteId, user)).thenReturn(Optional.of(existing));

        Note saved = createTestNote(noteId, "Updated Title", "Updated Content", true, false, user);
        when(noteRepository.save(any(Note.class))).thenReturn(saved);

        NoteResponse response = noteService.updateNote("test@test.com", noteId, request);

        assertEquals("Updated Title", response.title());
        assertTrue(response.pinned());
    }

    @Test
    void deleteNote_shouldDeleteNote() {
        UUID noteId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        Note note = createTestNote(noteId, "Test Note", "Content", false, false, user);
        when(noteRepository.findByIdAndUser(noteId, user)).thenReturn(Optional.of(note));

        noteService.deleteNote("test@test.com", noteId);

        verify(noteRepository).delete(note);
    }

    @Test
    void togglePin_shouldFlipPinnedStatus() {
        UUID noteId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        Note note = createTestNote(noteId, "Test Note", "Content", false, false, user);
        when(noteRepository.findByIdAndUser(noteId, user)).thenReturn(Optional.of(note));

        Note saved = createTestNote(noteId, "Test Note", "Content", true, false, user);
        when(noteRepository.save(any(Note.class))).thenReturn(saved);

        NoteResponse response = noteService.togglePin("test@test.com", noteId);

        assertTrue(response.pinned());
    }

    @Test
    void toggleArchive_shouldFlipArchivedStatus() {
        UUID noteId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        Note note = createTestNote(noteId, "Test Note", "Content", false, false, user);
        when(noteRepository.findByIdAndUser(noteId, user)).thenReturn(Optional.of(note));

        Note saved = createTestNote(noteId, "Test Note", "Content", false, true, user);
        when(noteRepository.save(any(Note.class))).thenReturn(saved);

        NoteResponse response = noteService.toggleArchive("test@test.com", noteId);

        assertTrue(response.archived());
    }

    @Test
    void searchNotes_shouldSearchByQuery() {
        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        Note note = createTestNote(UUID.randomUUID(), "Test Note", "Content", false, false, user);
        Page<Note> page = new PageImpl<>(List.of(note));
        when(noteRepository.searchByUserAndQuery(eq(user), eq("test"), eq(false), any(Pageable.class))).thenReturn(page);

        PaginatedResponse<NoteResponse> response = noteService.searchNotes("test@test.com", "test", false, 0, 10);

        assertEquals(1, response.content().size());
    }

    @Test
    void searchNotes_shouldFallbackToGetNotesWhenQueryBlank() {
        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        Note note = createTestNote(UUID.randomUUID(), "Test Note", "Content", false, false, user);
        Page<Note> page = new PageImpl<>(List.of(note));
        when(noteRepository.findByUserAndArchived(eq(user), eq(false), any(Pageable.class))).thenReturn(page);

        PaginatedResponse<NoteResponse> response = noteService.searchNotes("test@test.com", "", false, 0, 10);

        assertEquals(1, response.content().size());
        verify(noteRepository, never()).searchByUserAndQuery(any(), any(), anyBoolean(), any());
    }

    private Note createTestNote(UUID id, String title, String content, boolean pinned, boolean archived, User user) {
        Note note = new Note();
        note.setId(id);
        note.setTitle(title);
        note.setContent(content);
        note.setPinned(pinned);
        note.setArchived(archived);
        note.setUser(user);
        note.setLabels(new HashSet<>());
        return note;
    }
}
