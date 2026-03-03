package com.personalspace.api.repository;

import com.personalspace.api.model.entity.Note;
import com.personalspace.api.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {

    Optional<Note> findByIdAndUser(UUID id, User user);

    Page<Note> findByUserAndArchived(User user, boolean archived, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.user = :user AND n.archived = :archived " +
            "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(n.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Note> searchByUserAndQuery(
            @Param("user") User user,
            @Param("query") String query,
            @Param("archived") boolean archived,
            Pageable pageable);
}
