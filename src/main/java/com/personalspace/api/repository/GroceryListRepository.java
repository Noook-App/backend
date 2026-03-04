package com.personalspace.api.repository;

import com.personalspace.api.model.entity.GroceryList;
import com.personalspace.api.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GroceryListRepository extends JpaRepository<GroceryList, UUID> {

    Optional<GroceryList> findByIdAndUser(UUID id, User user);

    Page<GroceryList> findByUserAndArchived(User user, boolean archived, Pageable pageable);
}
