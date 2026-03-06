package com.personalspace.api.repository;

import com.personalspace.api.model.entity.GroceryList;
import com.personalspace.api.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GroceryListRepository extends JpaRepository<GroceryList, UUID> {

    Optional<GroceryList> findByIdAndUser(UUID id, User user);

    Page<GroceryList> findByUserAndArchived(User user, boolean archived, Pageable pageable);

    @Query("SELECT DISTINCT g FROM GroceryList g LEFT JOIN g.items i WHERE g.user = :user AND g.archived = :archived " +
            "AND (LOWER(g.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<GroceryList> searchByUserAndQuery(
            @Param("user") User user,
            @Param("query") String query,
            @Param("archived") boolean archived,
            Pageable pageable);
}
