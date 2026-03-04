package com.personalspace.api.repository;

import com.personalspace.api.model.entity.GroceryItem;
import com.personalspace.api.model.entity.GroceryList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroceryItemRepository extends JpaRepository<GroceryItem, UUID> {

    Optional<GroceryItem> findByIdAndGroceryList(UUID id, GroceryList groceryList);

    List<GroceryItem> findByGroceryListOrderByCheckedAscCreatedAtAsc(GroceryList groceryList);

    boolean existsByGroceryListAndCheckedFalse(GroceryList groceryList);
}
