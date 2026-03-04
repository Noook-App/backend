package com.personalspace.api.repository;

import com.personalspace.api.model.entity.GroceryList;
import com.personalspace.api.model.entity.User;
import com.personalspace.api.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class GroceryListRepositoryTest {

    @Autowired
    private GroceryListRepository groceryListRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        groceryListRepository.deleteAll();
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

        GroceryList list1 = new GroceryList();
        list1.setTitle("Weekly Groceries");
        list1.setUser(user);
        groceryListRepository.save(list1);

        GroceryList list2 = new GroceryList();
        list2.setTitle("Archived List");
        list2.setArchived(true);
        list2.setUser(user);
        groceryListRepository.save(list2);

        GroceryList list3 = new GroceryList();
        list3.setTitle("Other User List");
        list3.setUser(otherUser);
        groceryListRepository.save(list3);
    }

    @Test
    void findByIdAndUser_shouldReturnListWhenOwned() {
        GroceryList list = groceryListRepository.findByUserAndArchived(user, false, PageRequest.of(0, 10)).getContent().get(0);
        Optional<GroceryList> found = groceryListRepository.findByIdAndUser(list.getId(), user);
        assertTrue(found.isPresent());
        assertEquals("Weekly Groceries", found.get().getTitle());
    }

    @Test
    void findByIdAndUser_shouldReturnEmptyWhenNotOwned() {
        GroceryList otherList = groceryListRepository.findByUserAndArchived(otherUser, false, PageRequest.of(0, 10)).getContent().get(0);
        Optional<GroceryList> found = groceryListRepository.findByIdAndUser(otherList.getId(), user);
        assertFalse(found.isPresent());
    }

    @Test
    void findByUserAndArchived_shouldFilterByArchivedFlag() {
        Page<GroceryList> activeLists = groceryListRepository.findByUserAndArchived(user, false, PageRequest.of(0, 10));
        assertEquals(1, activeLists.getTotalElements());
        assertEquals("Weekly Groceries", activeLists.getContent().get(0).getTitle());

        Page<GroceryList> archivedLists = groceryListRepository.findByUserAndArchived(user, true, PageRequest.of(0, 10));
        assertEquals(1, archivedLists.getTotalElements());
        assertEquals("Archived List", archivedLists.getContent().get(0).getTitle());
    }

    @Test
    void findByUserAndArchived_shouldSupportPagination() {
        for (int i = 0; i < 15; i++) {
            GroceryList list = new GroceryList();
            list.setTitle("List " + i);
            list.setUser(user);
            groceryListRepository.save(list);
        }

        Page<GroceryList> firstPage = groceryListRepository.findByUserAndArchived(user, false, PageRequest.of(0, 10));
        assertEquals(10, firstPage.getContent().size());
        assertEquals(16, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
    }
}
