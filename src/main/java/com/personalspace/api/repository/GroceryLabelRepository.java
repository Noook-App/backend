package com.personalspace.api.repository;

import com.personalspace.api.model.entity.GroceryLabel;
import com.personalspace.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroceryLabelRepository extends JpaRepository<GroceryLabel, UUID> {

    List<GroceryLabel> findAllByUserOrderByNameAsc(User user);

    boolean existsByNameAndUser(String name, User user);

    Optional<GroceryLabel> findByIdAndUser(UUID id, User user);
}
