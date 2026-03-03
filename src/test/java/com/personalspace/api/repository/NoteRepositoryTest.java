package com.personalspace.api.repository;

import com.personalspace.api.model.entity.Note;
import com.personalspace.api.model.entity.User;
import com.personalspace.api.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NoteRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setName("Test User");
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
        user = userRepository.save(user);

        otherUser = new User();
        otherUser.setName("Other User");
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("encodedPassword");
        otherUser.setRole(Role.USER);
        otherUser = userRepository.save(otherUser);

        Note note1 = new Note();
        note1.setTitle("Active Note");
        note1.setContent("Some content");
        note1.setUser(user);
        noteRepository.save(note1);

        Note note2 = new Note();
        note2.setTitle("Archived Note");
        note2.setContent("Archived content");
        note2.setArchived(true);
        note2.setUser(user);
        noteRepository.save(note2);

        Note note3 = new Note();
        note3.setTitle("Other User Note");
        note3.setContent("Other content");
        note3.setUser(otherUser);
        noteRepository.save(note3);
    }

    @Test
    void findByIdAndUser_shouldReturnNoteWhenOwned() {
        Note note = noteRepository.findByUserAndArchived(user, false, PageRequest.of(0, 10)).getContent().get(0);
        Optional<Note> found = noteRepository.findByIdAndUser(note.getId(), user);
        assertTrue(found.isPresent());
        assertEquals("Active Note", found.get().getTitle());
    }

    @Test
    void findByIdAndUser_shouldReturnEmptyWhenNotOwned() {
        Note otherNote = noteRepository.findByUserAndArchived(otherUser, false, PageRequest.of(0, 10)).getContent().get(0);
        Optional<Note> found = noteRepository.findByIdAndUser(otherNote.getId(), user);
        assertFalse(found.isPresent());
    }

    @Test
    void findByUserAndArchived_shouldFilterByArchivedFlag() {
        Page<Note> activeNotes = noteRepository.findByUserAndArchived(user, false, PageRequest.of(0, 10));
        assertEquals(1, activeNotes.getTotalElements());
        assertEquals("Active Note", activeNotes.getContent().get(0).getTitle());

        Page<Note> archivedNotes = noteRepository.findByUserAndArchived(user, true, PageRequest.of(0, 10));
        assertEquals(1, archivedNotes.getTotalElements());
        assertEquals("Archived Note", archivedNotes.getContent().get(0).getTitle());
    }

    @Test
    void findByUserAndArchived_shouldSupportPagination() {
        // Add more notes
        for (int i = 0; i < 15; i++) {
            Note note = new Note();
            note.setTitle("Note " + i);
            note.setContent("Content " + i);
            note.setUser(user);
            noteRepository.save(note);
        }

        Page<Note> firstPage = noteRepository.findByUserAndArchived(user, false, PageRequest.of(0, 10));
        assertEquals(10, firstPage.getContent().size());
        assertEquals(16, firstPage.getTotalElements()); // 15 new + 1 existing active
        assertEquals(2, firstPage.getTotalPages());
    }

    @Test
    void searchByUserAndQuery_shouldMatchTitleCaseInsensitive() {
        Page<Note> results = noteRepository.searchByUserAndQuery(
                user, "active", false,
                PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt"))));
        assertEquals(1, results.getTotalElements());
        assertEquals("Active Note", results.getContent().get(0).getTitle());
    }

    @Test
    void searchByUserAndQuery_shouldMatchContent() {
        Page<Note> results = noteRepository.searchByUserAndQuery(
                user, "some content", false,
                PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt"))));
        assertEquals(1, results.getTotalElements());
    }

    @Test
    void searchByUserAndQuery_shouldNotReturnOtherUsersNotes() {
        Page<Note> results = noteRepository.searchByUserAndQuery(
                user, "other", false,
                PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt"))));
        assertEquals(0, results.getTotalElements());
    }
}
