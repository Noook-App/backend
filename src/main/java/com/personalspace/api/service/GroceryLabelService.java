package com.personalspace.api.service;

import com.personalspace.api.dto.request.CreateGroceryLabelRequest;
import com.personalspace.api.dto.request.UpdateGroceryLabelRequest;
import com.personalspace.api.dto.response.GroceryLabelResponse;
import com.personalspace.api.exception.DuplicateGroceryLabelException;
import com.personalspace.api.exception.ResourceNotFoundException;
import com.personalspace.api.model.entity.GroceryLabel;
import com.personalspace.api.model.entity.User;
import com.personalspace.api.repository.GroceryLabelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GroceryLabelService {

    private final GroceryLabelRepository groceryLabelRepository;
    private final UserService userService;

    public GroceryLabelService(GroceryLabelRepository groceryLabelRepository, UserService userService) {
        this.groceryLabelRepository = groceryLabelRepository;
        this.userService = userService;
    }

    @Transactional
    public GroceryLabelResponse createLabel(String email, CreateGroceryLabelRequest request) {
        User user = userService.getUserByEmail(email);

        if (groceryLabelRepository.existsByNameAndUser(request.name(), user)) {
            throw new DuplicateGroceryLabelException("Label already exists: " + request.name());
        }

        GroceryLabel label = new GroceryLabel();
        label.setName(request.name());
        label.setUser(user);

        GroceryLabel saved = groceryLabelRepository.save(label);
        return toGroceryLabelResponse(saved);
    }

    public List<GroceryLabelResponse> getLabels(String email) {
        User user = userService.getUserByEmail(email);
        return groceryLabelRepository.findAllByUserOrderByNameAsc(user).stream()
                .map(this::toGroceryLabelResponse)
                .toList();
    }

    @Transactional
    public GroceryLabelResponse updateLabel(String email, UUID labelId, UpdateGroceryLabelRequest request) {
        User user = userService.getUserByEmail(email);
        GroceryLabel label = groceryLabelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + labelId));

        if (!label.getName().equals(request.name()) && groceryLabelRepository.existsByNameAndUser(request.name(), user)) {
            throw new DuplicateGroceryLabelException("Label already exists: " + request.name());
        }

        label.setName(request.name());
        GroceryLabel saved = groceryLabelRepository.save(label);
        return toGroceryLabelResponse(saved);
    }

    @Transactional
    public void deleteLabel(String email, UUID labelId) {
        User user = userService.getUserByEmail(email);
        GroceryLabel label = groceryLabelRepository.findByIdAndUser(labelId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + labelId));

        label.getGroceryLists().forEach(list -> list.getLabels().remove(label));
        label.getGroceryItems().forEach(item -> item.getLabels().remove(label));

        groceryLabelRepository.delete(label);
    }

    private GroceryLabelResponse toGroceryLabelResponse(GroceryLabel label) {
        return new GroceryLabelResponse(label.getId(), label.getName(), label.getCreatedAt());
    }
}
