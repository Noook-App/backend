package com.personalspace.api.repository;

import com.personalspace.api.model.entity.GroceryItem;
import com.personalspace.api.model.entity.GroceryList;
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
class GroceryItemRepositoryTest {

    @Autowired
    private GroceryItemRepository groceryItemRepository;

    @Autowired
    private GroceryListRepository groceryListRepository;

    @Autowired
    private UserRepository userRepository;

    private GroceryList groceryList;

    @BeforeEach
    void setUp() {
        groceryItemRepository.deleteAll();
        groceryListRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("Test User");
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
        user = userRepository.save(user);

        groceryList = new GroceryList();
        groceryList.setTitle("Weekly Groceries");
        groceryList.setUser(user);
        groceryList = groceryListRepository.save(groceryList);

        GroceryItem item1 = new GroceryItem();
        item1.setName("Apples");
        item1.setChecked(false);
        item1.setGroceryList(groceryList);
        groceryItemRepository.save(item1);

        GroceryItem item2 = new GroceryItem();
        item2.setName("Bread");
        item2.setChecked(true);
        item2.setGroceryList(groceryList);
        groceryItemRepository.save(item2);

        GroceryItem item3 = new GroceryItem();
        item3.setName("Milk");
        item3.setChecked(false);
        item3.setGroceryList(groceryList);
        groceryItemRepository.save(item3);
    }

    @Test
    void findByIdAndGroceryList_shouldReturnItemWhenBelongsToList() {
        GroceryItem item = groceryItemRepository.findByGroceryListOrderByCheckedAscCreatedAtAsc(groceryList).get(0);
        Optional<GroceryItem> found = groceryItemRepository.findByIdAndGroceryList(item.getId(), groceryList);
        assertTrue(found.isPresent());
    }

    @Test
    void findByGroceryListOrderByCheckedAscCreatedAtAsc_shouldReturnUncheckedFirst() {
        List<GroceryItem> items = groceryItemRepository.findByGroceryListOrderByCheckedAscCreatedAtAsc(groceryList);
        assertEquals(3, items.size());
        assertFalse(items.get(0).isChecked());
        assertFalse(items.get(1).isChecked());
        assertTrue(items.get(2).isChecked());
    }

    @Test
    void existsByGroceryListAndCheckedFalse_shouldReturnTrueWhenUncheckedExist() {
        assertTrue(groceryItemRepository.existsByGroceryListAndCheckedFalse(groceryList));
    }

    @Test
    void existsByGroceryListAndCheckedFalse_shouldReturnFalseWhenAllChecked() {
        List<GroceryItem> items = groceryItemRepository.findByGroceryListOrderByCheckedAscCreatedAtAsc(groceryList);
        items.forEach(item -> item.setChecked(true));
        groceryItemRepository.saveAll(items);

        assertFalse(groceryItemRepository.existsByGroceryListAndCheckedFalse(groceryList));
    }
}
