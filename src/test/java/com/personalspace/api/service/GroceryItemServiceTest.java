package com.personalspace.api.service;

import com.personalspace.api.dto.request.CreateGroceryItemRequest;
import com.personalspace.api.dto.request.UpdateGroceryItemRequest;
import com.personalspace.api.dto.response.GroceryItemResponse;
import com.personalspace.api.exception.ResourceNotFoundException;
import com.personalspace.api.model.entity.GroceryItem;
import com.personalspace.api.model.entity.GroceryList;
import com.personalspace.api.model.entity.User;
import com.personalspace.api.repository.GroceryItemRepository;
import com.personalspace.api.repository.GroceryLabelRepository;
import com.personalspace.api.repository.GroceryListRepository;
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
class GroceryItemServiceTest {

    @Mock
    private GroceryItemRepository groceryItemRepository;

    @Mock
    private GroceryListRepository groceryListRepository;

    @Mock
    private GroceryLabelRepository groceryLabelRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private GroceryItemService groceryItemService;

    private User user;
    private GroceryList groceryList;
    private UUID listId;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");

        listId = UUID.randomUUID();
        groceryList = new GroceryList();
        groceryList.setId(listId);
        groceryList.setTitle("Weekly Groceries");
        groceryList.setUser(user);
        groceryList.setArchived(false);
    }

    @Test
    void createItem_shouldReturnGroceryItemResponse() {
        CreateGroceryItemRequest request = new CreateGroceryItemRequest("Apples", "5", null);

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.of(groceryList));

        GroceryItem saved = createTestGroceryItem(UUID.randomUUID(), "Apples", "5", false);
        when(groceryItemRepository.save(any(GroceryItem.class))).thenReturn(saved);

        GroceryItemResponse response = groceryItemService.createItem("test@test.com", listId, request);

        assertNotNull(response);
        assertEquals("Apples", response.name());
        assertEquals("5", response.quantity());
    }

    @Test
    void createItem_shouldThrowWhenListNotFound() {
        CreateGroceryItemRequest request = new CreateGroceryItemRequest("Apples", "5", null);

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> groceryItemService.createItem("test@test.com", listId, request));
    }

    @Test
    void getItems_shouldReturnSortedItems() {
        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.of(groceryList));

        GroceryItem item1 = createTestGroceryItem(UUID.randomUUID(), "Apples", null, false);
        GroceryItem item2 = createTestGroceryItem(UUID.randomUUID(), "Bread", null, true);
        when(groceryItemRepository.findByGroceryListOrderByCheckedAscCreatedAtAsc(groceryList))
                .thenReturn(List.of(item1, item2));

        List<GroceryItemResponse> items = groceryItemService.getItems("test@test.com", listId);

        assertEquals(2, items.size());
        assertEquals("Apples", items.get(0).name());
        assertFalse(items.get(0).checked());
        assertEquals("Bread", items.get(1).name());
        assertTrue(items.get(1).checked());
    }

    @Test
    void updateItem_shouldUpdateFields() {
        UUID itemId = UUID.randomUUID();
        UpdateGroceryItemRequest request = new UpdateGroceryItemRequest("Updated Apples", "10", null);

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.of(groceryList));

        GroceryItem existing = createTestGroceryItem(itemId, "Apples", "5", false);
        when(groceryItemRepository.findByIdAndGroceryList(itemId, groceryList)).thenReturn(Optional.of(existing));

        GroceryItem saved = createTestGroceryItem(itemId, "Updated Apples", "10", false);
        when(groceryItemRepository.save(any(GroceryItem.class))).thenReturn(saved);

        GroceryItemResponse response = groceryItemService.updateItem("test@test.com", listId, itemId, request);

        assertEquals("Updated Apples", response.name());
        assertEquals("10", response.quantity());
    }

    @Test
    void deleteItem_shouldDeleteItem() {
        UUID itemId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.of(groceryList));

        GroceryItem item = createTestGroceryItem(itemId, "Apples", null, false);
        when(groceryItemRepository.findByIdAndGroceryList(itemId, groceryList)).thenReturn(Optional.of(item));

        groceryItemService.deleteItem("test@test.com", listId, itemId);

        verify(groceryItemRepository).delete(item);
    }

    @Test
    void toggleChecked_shouldFlipCheckedStatus() {
        UUID itemId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.of(groceryList));

        GroceryItem item = createTestGroceryItem(itemId, "Apples", null, false);
        when(groceryItemRepository.findByIdAndGroceryList(itemId, groceryList)).thenReturn(Optional.of(item));

        GroceryItem saved = createTestGroceryItem(itemId, "Apples", null, true);
        when(groceryItemRepository.save(any(GroceryItem.class))).thenReturn(saved);
        when(groceryItemRepository.existsByGroceryListAndCheckedFalse(groceryList)).thenReturn(true);

        GroceryItemResponse response = groceryItemService.toggleChecked("test@test.com", listId, itemId);

        assertTrue(response.checked());
        verify(groceryListRepository, never()).save(any(GroceryList.class));
    }

    @Test
    void toggleChecked_shouldAutoArchiveWhenAllItemsChecked() {
        UUID itemId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.of(groceryList));

        GroceryItem item = createTestGroceryItem(itemId, "Apples", null, false);
        when(groceryItemRepository.findByIdAndGroceryList(itemId, groceryList)).thenReturn(Optional.of(item));

        GroceryItem saved = createTestGroceryItem(itemId, "Apples", null, true);
        when(groceryItemRepository.save(any(GroceryItem.class))).thenReturn(saved);
        when(groceryItemRepository.existsByGroceryListAndCheckedFalse(groceryList)).thenReturn(false);

        groceryItemService.toggleChecked("test@test.com", listId, itemId);

        verify(groceryListRepository).save(groceryList);
        assertTrue(groceryList.isArchived());
    }

    @Test
    void toggleChecked_shouldNotAutoUnarchiveOnUncheck() {
        UUID itemId = UUID.randomUUID();
        groceryList.setArchived(true);

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.of(groceryList));

        GroceryItem item = createTestGroceryItem(itemId, "Apples", null, true);
        when(groceryItemRepository.findByIdAndGroceryList(itemId, groceryList)).thenReturn(Optional.of(item));

        GroceryItem saved = createTestGroceryItem(itemId, "Apples", null, false);
        when(groceryItemRepository.save(any(GroceryItem.class))).thenReturn(saved);

        groceryItemService.toggleChecked("test@test.com", listId, itemId);

        assertTrue(groceryList.isArchived());
        verify(groceryListRepository, never()).save(any(GroceryList.class));
    }

    private GroceryItem createTestGroceryItem(UUID id, String name, String quantity, boolean checked) {
        GroceryItem item = new GroceryItem();
        item.setId(id);
        item.setName(name);
        item.setQuantity(quantity);
        item.setChecked(checked);
        item.setGroceryList(groceryList);
        item.setLabels(new HashSet<>());
        return item;
    }
}
