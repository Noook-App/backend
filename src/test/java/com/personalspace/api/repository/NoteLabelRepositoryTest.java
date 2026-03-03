package com.personalspace.api.repository;

import com.personalspace.api.model.entity.NoteLabel;
import com.personalspace.api.model.entity.User;
import com.personalspace.api.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NoteLabelRepositoryTest {

    @Autowired
    private NoteLabelRepository noteLabelRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        noteLabelRepository.deleteAll();
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

        NoteLabel label1 = new NoteLabel();
        label1.setName("Work");
        label1.setUser(user);
        noteLabelRepository.save(label1);

        NoteLabel label2 = new NoteLabel();
        label2.setName("Personal");
        label2.setUser(user);
        noteLabelRepository.save(label2);

        NoteLabel label3 = new NoteLabel();
        label3.setName("Work");
        label3.setUser(otherUser);
        noteLabelRepository.save(label3);
    }

    @Test
    void findAllByUserOrderByNameAsc_shouldReturnLabelsInAlphabeticalOrder() {
        List<NoteLabel> labels = noteLabelRepository.findAllByUserOrderByNameAsc(user);
        assertEquals(2, labels.size());
        assertEquals("Personal", labels.get(0).getName());
        assertEquals("Work", labels.get(1).getName());
    }

    @Test
    void existsByNameAndUser_shouldReturnTrueWhenExists() {
        assertTrue(noteLabelRepository.existsByNameAndUser("Work", user));
    }

    @Test
    void existsByNameAndUser_shouldReturnFalseWhenNotExists() {
        assertFalse(noteLabelRepository.existsByNameAndUser("Nonexistent", user));
    }

    @Test
    void findByIdAndUser_shouldReturnLabelWhenOwned() {
        NoteLabel label = noteLabelRepository.findAllByUserOrderByNameAsc(user).get(0);
        Optional<NoteLabel> found = noteLabelRepository.findByIdAndUser(label.getId(), user);
        assertTrue(found.isPresent());
    }

    @Test
    void findByIdAndUser_shouldReturnEmptyWhenNotOwned() {
        NoteLabel otherLabel = noteLabelRepository.findAllByUserOrderByNameAsc(otherUser).get(0);
        Optional<NoteLabel> found = noteLabelRepository.findByIdAndUser(otherLabel.getId(), user);
        assertFalse(found.isPresent());
    }
}
