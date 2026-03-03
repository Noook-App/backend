package com.personalspace.api.repository;

import com.personalspace.api.model.entity.NoteLabel;
import com.personalspace.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NoteLabelRepository extends JpaRepository<NoteLabel, UUID> {

    List<NoteLabel> findAllByUserOrderByNameAsc(User user);

    boolean existsByNameAndUser(String name, User user);

    Optional<NoteLabel> findByIdAndUser(UUID id, User user);
}
