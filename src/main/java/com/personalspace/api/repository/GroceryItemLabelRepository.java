package com.personalspace.api.repository;

import com.personalspace.api.model.entity.GroceryItemLabel;
import com.personalspace.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroceryItemLabelRepository extends JpaRepository<GroceryItemLabel, UUID> {

    List<GroceryItemLabel> findAllByUserOrderByNameAsc(User user);

    boolean existsByNameAndUser(String name, User user);

    Optional<GroceryItemLabel> findByIdAndUser(UUID id, User user);
}
