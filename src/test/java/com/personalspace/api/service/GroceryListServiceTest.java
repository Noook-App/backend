package com.personalspace.api.service;

import com.personalspace.api.dto.request.CreateGroceryItemRequest;
import com.personalspace.api.dto.request.CreateGroceryListRequest;
import com.personalspace.api.dto.request.UpdateGroceryListRequest;
import com.personalspace.api.dto.response.GroceryListResponse;
import com.personalspace.api.dto.response.PaginatedResponse;
import com.personalspace.api.exception.ResourceNotFoundException;
import com.personalspace.api.model.entity.GroceryItem;
import com.personalspace.api.model.entity.GroceryLabel;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroceryListServiceTest {

    @Mock
    private GroceryListRepository groceryListRepository;

    @Mock
    private GroceryItemRepository groceryItemRepository;

    @Mock
    private GroceryLabelRepository groceryLabelRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private GroceryListService groceryListService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
    }

    @Test
    void createGroceryList_shouldReturnGroceryListResponse() {
        CreateGroceryListRequest request = new CreateGroceryListRequest("Weekly Groceries", null, null);

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        GroceryList saved = createTestGroceryList(UUID.randomUUID(), "Weekly Groceries", false, user);
        when(groceryListRepository.save(any(GroceryList.class))).thenReturn(saved);
        when(groceryItemRepository.findByGroceryListOrderByCheckedAscCreatedAtAsc(saved)).thenReturn(List.of());

        GroceryListResponse response = groceryListService.createGroceryList("test@test.com", request);

        assertNotNull(response);
        assertEquals("Weekly Groceries", response.title());
        assertFalse(response.archived());
    }

    @Test
    void createGroceryList_shouldCreateWithInlineItems() {
        List<CreateGroceryItemRequest> items = List.of(
                new CreateGroceryItemRequest("Apples", "5", null),
                new CreateGroceryItemRequest("Bread", null, null)
        );
        CreateGroceryListRequest request = new CreateGroceryListRequest("Weekly Groceries", items, null);

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        GroceryList saved = createTestGroceryList(UUID.randomUUID(), "Weekly Groceries", false, user);
        when(groceryListRepository.save(any(GroceryList.class))).thenReturn(saved);
        when(groceryItemRepository.findByGroceryListOrderByCheckedAscCreatedAtAsc(saved)).thenReturn(List.of());

        GroceryListResponse response = groceryListService.createGroceryList("test@test.com", request);

        assertNotNull(response);
        verify(groceryListRepository).save(any(GroceryList.class));
    }

    @Test
    void getGroceryLists_shouldReturnPaginatedResponse() {
        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        GroceryList list = createTestGroceryList(UUID.randomUUID(), "Weekly Groceries", false, user);
        Page<GroceryList> page = new PageImpl<>(List.of(list));
        when(groceryListRepository.findByUserAndArchived(eq(user), eq(false), any(Pageable.class))).thenReturn(page);
        when(groceryItemRepository.findByGroceryListOrderByCheckedAscCreatedAtAsc(list)).thenReturn(List.of());

        PaginatedResponse<GroceryListResponse> response = groceryListService.getGroceryLists("test@test.com", false, 0, 10);

        assertEquals(1, response.content().size());
        assertEquals(1, response.totalElements());
    }

    @Test
    void getGroceryList_shouldReturnGroceryListResponse() {
        UUID listId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        GroceryList list = createTestGroceryList(listId, "Weekly Groceries", false, user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.of(list));
        when(groceryItemRepository.findByGroceryListOrderByCheckedAscCreatedAtAsc(list)).thenReturn(List.of());

        GroceryListResponse response = groceryListService.getGroceryList("test@test.com", listId);

        assertEquals("Weekly Groceries", response.title());
    }

    @Test
    void getGroceryList_shouldThrowWhenNotFound() {
        UUID listId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> groceryListService.getGroceryList("test@test.com", listId));
    }

    @Test
    void updateGroceryList_shouldUpdateFields() {
        UUID listId = UUID.randomUUID();
        UpdateGroceryListRequest request = new UpdateGroceryListRequest("Updated Title", null);

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        GroceryList existing = createTestGroceryList(listId, "Old Title", false, user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.of(existing));

        GroceryList saved = createTestGroceryList(listId, "Updated Title", false, user);
        when(groceryListRepository.save(any(GroceryList.class))).thenReturn(saved);
        when(groceryItemRepository.findByGroceryListOrderByCheckedAscCreatedAtAsc(saved)).thenReturn(List.of());

        GroceryListResponse response = groceryListService.updateGroceryList("test@test.com", listId, request);

        assertEquals("Updated Title", response.title());
    }

    @Test
    void deleteGroceryList_shouldDeleteList() {
        UUID listId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        GroceryList list = createTestGroceryList(listId, "Weekly Groceries", false, user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.of(list));

        groceryListService.deleteGroceryList("test@test.com", listId);

        verify(groceryListRepository).delete(list);
    }

    @Test
    void toggleArchive_shouldFlipArchivedStatus() {
        UUID listId = UUID.randomUUID();

        when(userService.getUserByEmail("test@test.com")).thenReturn(user);

        GroceryList list = createTestGroceryList(listId, "Weekly Groceries", false, user);
        when(groceryListRepository.findByIdAndUser(listId, user)).thenReturn(Optional.of(list));

        GroceryList saved = createTestGroceryList(listId, "Weekly Groceries", true, user);
        when(groceryListRepository.save(any(GroceryList.class))).thenReturn(saved);
        when(groceryItemRepository.findByGroceryListOrderByCheckedAscCreatedAtAsc(saved)).thenReturn(List.of());

        GroceryListResponse response = groceryListService.toggleArchive("test@test.com", listId);

        assertTrue(response.archived());
    }

    private GroceryList createTestGroceryList(UUID id, String title, boolean archived, User user) {
        GroceryList list = new GroceryList();
        list.setId(id);
        list.setTitle(title);
        list.setArchived(archived);
        list.setUser(user);
        list.setLabels(new HashSet<>());
        list.setItems(new ArrayList<>());
        return list;
    }
}
