package com.personalspace.api.controller;

import com.personalspace.api.dto.request.CreateGroceryLabelRequest;
import com.personalspace.api.dto.request.UpdateGroceryLabelRequest;
import com.personalspace.api.dto.response.GroceryLabelResponse;
import com.personalspace.api.service.GroceryLabelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/grocery-lists/labels")
public class GroceryLabelController {

    private final GroceryLabelService groceryLabelService;

    public GroceryLabelController(GroceryLabelService groceryLabelService) {
        this.groceryLabelService = groceryLabelService;
    }

    @PostMapping
    public ResponseEntity<GroceryLabelResponse> createLabel(
            Principal principal,
            @Valid @RequestBody CreateGroceryLabelRequest request) {
        GroceryLabelResponse response = groceryLabelService.createLabel(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GroceryLabelResponse>> getLabels(Principal principal) {
        List<GroceryLabelResponse> response = groceryLabelService.getLabels(principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroceryLabelResponse> updateLabel(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGroceryLabelRequest request) {
        GroceryLabelResponse response = groceryLabelService.updateLabel(principal.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabel(
            Principal principal,
            @PathVariable UUID id) {
        groceryLabelService.deleteLabel(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
