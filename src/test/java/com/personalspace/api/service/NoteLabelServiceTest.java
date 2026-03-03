package com.personalspace.api.service;

import com.personalspace.api.dto.request.CreateNoteLabelRequest;
import com.personalspace.api.dto.request.UpdateNoteLabelRequest;
import com.personalspace.api.dto.response.NoteLabelResponse;
import com.personalspace.api.exception.DuplicateNoteLabelException;
import com.personalspace.api.exception.ResourceNotFoundException;
import com.personalspace.api.model.entity.NoteLabel;
import com.personalspace.api.model.entity.Note;
import com.personalspace.api.model.entity.User;
import com.personalspace.api.repository.NoteLabelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteLabelServiceTest {

    @Mock
    private NoteLabelRepository noteLabelRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NoteLabelService noteLabelService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
    }

    @Test
    void createLabel_shouldReturnNoteLabelResponse() {
        CreateNoteLabelRequest request = new CreateNoteLabelRequest("Work");

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(noteLabelRepository.existsByNameAndUser("Work", user)).thenReturn(false);

        NoteLabel saved = createTestNoteLabel(UUID.randomUUID(), "Work", user);
        when(noteLabelRepository.save(any(NoteLabel.class))).thenReturn(saved);

        NoteLabelResponse response = noteLabelService.createLabel("test@test.com", request);

        assertNotNull(response);
        assertEquals("Work", response.name());
        verify(noteLabelRepository).save(any(NoteLabel.class));
    }

    @Test
    void createLabel_shouldThrowWhenDuplicate() {
        CreateNoteLabelRequest request = new CreateNoteLabelRequest("Work");

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(noteLabelRepository.existsByNameAndUser("Work", user)).thenReturn(true);

        assertThrows(DuplicateNoteLabelException.class, () -> noteLabelService.createLabel("test@test.com", request));
        verify(noteLabelRepository, never()).save(any(NoteLabel.class));
    }

    @Test
    void getLabels_shouldReturnAllLabelsForUser() {
        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        NoteLabel label1 = createTestNoteLabel(UUID.randomUUID(), "Personal", user);
        NoteLabel label2 = createTestNoteLabel(UUID.randomUUID(), "Work", user);
        when(noteLabelRepository.findAllByUserOrderByNameAsc(user)).thenReturn(List.of(label1, label2));

        List<NoteLabelResponse> labels = noteLabelService.getLabels("test@test.com");

        assertEquals(2, labels.size());
        assertEquals("Personal", labels.get(0).name());
        assertEquals("Work", labels.get(1).name());
    }

    @Test
    void updateLabel_shouldUpdateName() {
        UUID labelId = UUID.randomUUID();
        UpdateNoteLabelRequest request = new UpdateNoteLabelRequest("Updated");

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        NoteLabel existing = createTestNoteLabel(labelId, "Old", user);
        when(noteLabelRepository.findByIdAndUser(labelId, user)).thenReturn(Optional.of(existing));
        when(noteLabelRepository.existsByNameAndUser("Updated", user)).thenReturn(false);

        NoteLabel updated = createTestNoteLabel(labelId, "Updated", user);
        when(noteLabelRepository.save(any(NoteLabel.class))).thenReturn(updated);

        NoteLabelResponse response = noteLabelService.updateLabel("test@test.com", labelId, request);

        assertEquals("Updated", response.name());
    }

    @Test
    void updateLabel_shouldThrowWhenNotFound() {
        UUID labelId = UUID.randomUUID();
        UpdateNoteLabelRequest request = new UpdateNoteLabelRequest("Updated");

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(noteLabelRepository.findByIdAndUser(labelId, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> noteLabelService.updateLabel("test@test.com", labelId, request));
    }

    @Test
    void updateLabel_shouldThrowWhenDuplicateName() {
        UUID labelId = UUID.randomUUID();
        UpdateNoteLabelRequest request = new UpdateNoteLabelRequest("Existing");

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        NoteLabel existing = createTestNoteLabel(labelId, "Old", user);
        when(noteLabelRepository.findByIdAndUser(labelId, user)).thenReturn(Optional.of(existing));
        when(noteLabelRepository.existsByNameAndUser("Existing", user)).thenReturn(true);

        assertThrows(DuplicateNoteLabelException.class,
                () -> noteLabelService.updateLabel("test@test.com", labelId, request));
    }

    @Test
    void deleteLabel_shouldDeleteLabel() {
        UUID labelId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        NoteLabel label = createTestNoteLabel(labelId, "Work", user);
        label.setNotes(new HashSet<>());
        when(noteLabelRepository.findByIdAndUser(labelId, user)).thenReturn(Optional.of(label));

        noteLabelService.deleteLabel("test@test.com", labelId);

        verify(noteLabelRepository).delete(label);
    }

    @Test
    void deleteLabel_shouldThrowWhenNotFound() {
        UUID labelId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(noteLabelRepository.findByIdAndUser(labelId, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> noteLabelService.deleteLabel("test@test.com", labelId));
    }

    private NoteLabel createTestNoteLabel(UUID id, String name, User user) {
        NoteLabel label = new NoteLabel();
        label.setId(id);
        label.setName(name);
        label.setUser(user);
        return label;
    }
}
