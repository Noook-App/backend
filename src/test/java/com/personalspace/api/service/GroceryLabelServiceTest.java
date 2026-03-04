package com.personalspace.api.service;

import com.personalspace.api.dto.request.CreateGroceryLabelRequest;
import com.personalspace.api.dto.request.UpdateGroceryLabelRequest;
import com.personalspace.api.dto.response.GroceryLabelResponse;
import com.personalspace.api.exception.DuplicateGroceryLabelException;
import com.personalspace.api.exception.ResourceNotFoundException;
import com.personalspace.api.model.entity.GroceryItem;
import com.personalspace.api.model.entity.GroceryLabel;
import com.personalspace.api.model.entity.GroceryList;
import com.personalspace.api.model.entity.User;
import com.personalspace.api.repository.GroceryLabelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroceryLabelServiceTest {

    @Mock
    private GroceryLabelRepository groceryLabelRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private GroceryLabelService groceryLabelService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
    }

    @Test
    void createLabel_shouldReturnGroceryLabelResponse() {
        CreateGroceryLabelRequest request = new CreateGroceryLabelRequest("Produce");

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryLabelRepository.existsByNameAndUser("Produce", user)).thenReturn(false);

        GroceryLabel saved = createTestGroceryLabel(UUID.randomUUID(), "Produce", user);
        when(groceryLabelRepository.save(any(GroceryLabel.class))).thenReturn(saved);

        GroceryLabelResponse response = groceryLabelService.createLabel("test@test.com", request);

        assertNotNull(response);
        assertEquals("Produce", response.name());
        verify(groceryLabelRepository).save(any(GroceryLabel.class));
    }

    @Test
    void createLabel_shouldThrowWhenDuplicate() {
        CreateGroceryLabelRequest request = new CreateGroceryLabelRequest("Produce");

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryLabelRepository.existsByNameAndUser("Produce", user)).thenReturn(true);

        assertThrows(DuplicateGroceryLabelException.class, () -> groceryLabelService.createLabel("test@test.com", request));
        verify(groceryLabelRepository, never()).save(any(GroceryLabel.class));
    }

    @Test
    void getLabels_shouldReturnAllLabelsForUser() {
        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        GroceryLabel label1 = createTestGroceryLabel(UUID.randomUUID(), "Bakery", user);
        GroceryLabel label2 = createTestGroceryLabel(UUID.randomUUID(), "Produce", user);
        when(groceryLabelRepository.findAllByUserOrderByNameAsc(user)).thenReturn(List.of(label1, label2));

        List<GroceryLabelResponse> labels = groceryLabelService.getLabels("test@test.com");

        assertEquals(2, labels.size());
        assertEquals("Bakery", labels.get(0).name());
        assertEquals("Produce", labels.get(1).name());
    }

    @Test
    void updateLabel_shouldUpdateName() {
        UUID labelId = UUID.randomUUID();
        UpdateGroceryLabelRequest request = new UpdateGroceryLabelRequest("Updated");

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        GroceryLabel existing = createTestGroceryLabel(labelId, "Old", user);
        when(groceryLabelRepository.findByIdAndUser(labelId, user)).thenReturn(Optional.of(existing));
        when(groceryLabelRepository.existsByNameAndUser("Updated", user)).thenReturn(false);

        GroceryLabel updated = createTestGroceryLabel(labelId, "Updated", user);
        when(groceryLabelRepository.save(any(GroceryLabel.class))).thenReturn(updated);

        GroceryLabelResponse response = groceryLabelService.updateLabel("test@test.com", labelId, request);

        assertEquals("Updated", response.name());
    }

    @Test
    void updateLabel_shouldThrowWhenNotFound() {
        UUID labelId = UUID.randomUUID();
        UpdateGroceryLabelRequest request = new UpdateGroceryLabelRequest("Updated");

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryLabelRepository.findByIdAndUser(labelId, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> groceryLabelService.updateLabel("test@test.com", labelId, request));
    }

    @Test
    void updateLabel_shouldThrowWhenDuplicateName() {
        UUID labelId = UUID.randomUUID();
        UpdateGroceryLabelRequest request = new UpdateGroceryLabelRequest("Existing");

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        GroceryLabel existing = createTestGroceryLabel(labelId, "Old", user);
        when(groceryLabelRepository.findByIdAndUser(labelId, user)).thenReturn(Optional.of(existing));
        when(groceryLabelRepository.existsByNameAndUser("Existing", user)).thenReturn(true);

        assertThrows(DuplicateGroceryLabelException.class,
                () -> groceryLabelService.updateLabel("test@test.com", labelId, request));
    }

    @Test
    void deleteLabel_shouldCleanupAndDelete() {
        UUID labelId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        GroceryLabel label = createTestGroceryLabel(labelId, "Produce", user);

        GroceryList list = new GroceryList();
        list.setLabels(new HashSet<>(Set.of(label)));
        label.setGroceryLists(new HashSet<>(Set.of(list)));

        GroceryItem item = new GroceryItem();
        item.setLabels(new HashSet<>(Set.of(label)));
        label.setGroceryItems(new HashSet<>(Set.of(item)));

        when(groceryLabelRepository.findByIdAndUser(labelId, user)).thenReturn(Optional.of(label));

        groceryLabelService.deleteLabel("test@test.com", labelId);

        assertFalse(list.getLabels().contains(label));
        assertFalse(item.getLabels().contains(label));
        verify(groceryLabelRepository).delete(label);
    }

    @Test
    void deleteLabel_shouldThrowWhenNotFound() {
        UUID labelId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryLabelRepository.findByIdAndUser(labelId, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> groceryLabelService.deleteLabel("test@test.com", labelId));
    }

    private GroceryLabel createTestGroceryLabel(UUID id, String name, User user) {
        GroceryLabel label = new GroceryLabel();
        label.setId(id);
        label.setName(name);
        label.setUser(user);
        return label;
    }
}
