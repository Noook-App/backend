package com.personalspace.api.service;

import com.personalspace.api.dto.request.CreateGroceryItemLabelRequest;
import com.personalspace.api.dto.request.UpdateGroceryItemLabelRequest;
import com.personalspace.api.dto.response.GroceryItemLabelResponse;
import com.personalspace.api.exception.DuplicateGroceryItemLabelException;
import com.personalspace.api.exception.ResourceNotFoundException;
import com.personalspace.api.model.entity.GroceryItemLabel;
import com.personalspace.api.model.entity.User;
import com.personalspace.api.repository.GroceryItemLabelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GroceryItemLabelService {

    private final GroceryItemLabelRepository groceryItemLabelRepository;
    private final UserService userService;

    public GroceryItemLabelService(GroceryItemLabelRepository groceryItemLabelRepository,
                                   UserService userService) {
        this.groceryItemLabelRepository = groceryItemLabelRepository;
        this.userService = userService;
    }

    @Transactional
    public GroceryItemLabelResponse createLabel(String email, CreateGroceryItemLabelRequest request) {
        User user = userService.getUserByEmail(email);

        if (groceryItemLabelRepository.existsByNameAndUser(request.name(), user)) {
            throw new DuplicateGroceryItemLabelException("Label already exists: " + request.name());
        }

        GroceryItemLabel label = new GroceryItemLabel();
        label.setName(request.name());
        label.setUser(user);

        GroceryItemLabel saved = groceryItemLabelRepository.save(label);
        return toResponse(saved);
    }

    public List<GroceryItemLabelResponse> getLabels(String email) {
        User user = userService.getUserByEmail(email);
        return groceryItemLabelRepository.findAllByUserOrderByNameAsc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public GroceryItemLabelResponse updateLabel(String email, UUID labelId,
                                                UpdateGroceryItemLabelRequest request) {
        User user = userService.getUserByEmail(email);
        GroceryItemLabel label = groceryItemLabelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + labelId));

        if (!label.getName().equals(request.name()) &&
                groceryItemLabelRepository.existsByNameAndUser(request.name(), user)) {
            throw new DuplicateGroceryItemLabelException("Label already exists: " + request.name());
        }

        label.setName(request.name());
        GroceryItemLabel saved = groceryItemLabelRepository.save(label);
        return toResponse(saved);
    }

    @Transactional
    public void deleteLabel(String email, UUID labelId) {
        User user = userService.getUserByEmail(email);
        GroceryItemLabel label = groceryItemLabelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + labelId));

        label.getGroceryItems().forEach(item -> item.getLabels().remove(label));

        groceryItemLabelRepository.delete(label);
    }

    private GroceryItemLabelResponse toResponse(GroceryItemLabel label) {
        return new GroceryItemLabelResponse(label.getId(), label.getName(), label.getCreatedAt());
    }
}
