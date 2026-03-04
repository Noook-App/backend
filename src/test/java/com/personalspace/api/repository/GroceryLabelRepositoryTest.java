package com.personalspace.api.repository;

import com.personalspace.api.model.entity.GroceryLabel;
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
class GroceryLabelRepositoryTest {

    @Autowired
    private GroceryLabelRepository groceryLabelRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        groceryLabelRepository.deleteAll();
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

        GroceryLabel label1 = new GroceryLabel();
        label1.setName("Produce");
        label1.setUser(user);
        groceryLabelRepository.save(label1);

        GroceryLabel label2 = new GroceryLabel();
        label2.setName("Bakery");
        label2.setUser(user);
        groceryLabelRepository.save(label2);

        GroceryLabel label3 = new GroceryLabel();
        label3.setName("Produce");
        label3.setUser(otherUser);
        groceryLabelRepository.save(label3);
    }

    @Test
    void findAllByUserOrderByNameAsc_shouldReturnLabelsInAlphabeticalOrder() {
        List<GroceryLabel> labels = groceryLabelRepository.findAllByUserOrderByNameAsc(user);
        assertEquals(2, labels.size());
        assertEquals("Bakery", labels.get(0).getName());
        assertEquals("Produce", labels.get(1).getName());
    }

    @Test
    void existsByNameAndUser_shouldReturnTrueWhenExists() {
        assertTrue(groceryLabelRepository.existsByNameAndUser("Produce", user));
    }

    @Test
    void existsByNameAndUser_shouldReturnFalseWhenNotExists() {
        assertFalse(groceryLabelRepository.existsByNameAndUser("Nonexistent", user));
    }

    @Test
    void findByIdAndUser_shouldReturnLabelWhenOwned() {
        GroceryLabel label = groceryLabelRepository.findAllByUserOrderByNameAsc(user).get(0);
        Optional<GroceryLabel> found = groceryLabelRepository.findByIdAndUser(label.getId(), user);
        assertTrue(found.isPresent());
    }

    @Test
    void findByIdAndUser_shouldReturnEmptyWhenNotOwned() {
        GroceryLabel otherLabel = groceryLabelRepository.findAllByUserOrderByNameAsc(otherUser).get(0);
        Optional<GroceryLabel> found = groceryLabelRepository.findByIdAndUser(otherLabel.getId(), user);
        assertFalse(found.isPresent());
    }
}
