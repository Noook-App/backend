package com.personalspace.api.service;

import com.personalspace.api.dto.request.CreateNoteLabelRequest;
import com.personalspace.api.dto.request.UpdateNoteLabelRequest;
import com.personalspace.api.dto.response.NoteLabelResponse;
import com.personalspace.api.exception.DuplicateNoteLabelException;
import com.personalspace.api.exception.ResourceNotFoundException;
import com.personalspace.api.model.entity.NoteLabel;
import com.personalspace.api.model.entity.User;
import com.personalspace.api.repository.NoteLabelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NoteLabelService {

    private final NoteLabelRepository noteLabelRepository;
    private final UserService userService;

    public NoteLabelService(NoteLabelRepository noteLabelRepository, UserService userService) {
        this.noteLabelRepository = noteLabelRepository;
        this.userService = userService;
    }

    @Transactional
    public NoteLabelResponse createLabel(String email, CreateNoteLabelRequest request) {
        User user = userService.getUserByEmail(email);

        if (noteLabelRepository.existsByNameAndUser(request.name(), user)) {
            throw new DuplicateNoteLabelException("Label already exists: " + request.name());
        }

        NoteLabel label = new NoteLabel();
        label.setName(request.name());
        label.setUser(user);

        NoteLabel saved = noteLabelRepository.save(label);
        return toNoteLabelResponse(saved);
    }

    public List<NoteLabelResponse> getLabels(String email) {
        User user = userService.getUserByEmail(email);
        return noteLabelRepository.findAllByUserOrderByNameAsc(user).stream()
                .map(this::toNoteLabelResponse)
                .toList();
    }

    @Transactional
    public NoteLabelResponse updateLabel(String email, UUID labelId, UpdateNoteLabelRequest request) {
        User user = userService.getUserByEmail(email);
        NoteLabel label = noteLabelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + labelId));

        if (!label.getName().equals(request.name()) && noteLabelRepository.existsByNameAndUser(request.name(), user)) {
            throw new DuplicateNoteLabelException("Label already exists: " + request.name());
        }

        label.setName(request.name());
        NoteLabel saved = noteLabelRepository.save(label);
        return toNoteLabelResponse(saved);
    }

    @Transactional
    public void deleteLabel(String email, UUID labelId) {
        User user = userService.getUserByEmail(email);
        NoteLabel label = noteLabelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + labelId));

        label.getNotes().forEach(note -> note.getLabels().remove(label));

        noteLabelRepository.delete(label);
    }

    private NoteLabelResponse toNoteLabelResponse(NoteLabel label) {
        return new NoteLabelResponse(label.getId(), label.getName(), label.getCreatedAt());
    }
}
