package com.personalspace.api.controller;

import com.personalspace.api.dto.request.CreateGroceryItemLabelRequest;
import com.personalspace.api.dto.request.UpdateGroceryItemLabelRequest;
import com.personalspace.api.dto.response.GroceryItemLabelResponse;
import com.personalspace.api.service.GroceryItemLabelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/grocery-lists/items/labels")
public class GroceryItemLabelController {

    private final GroceryItemLabelService groceryItemLabelService;

    public GroceryItemLabelController(GroceryItemLabelService groceryItemLabelService) {
        this.groceryItemLabelService = groceryItemLabelService;
    }

    @PostMapping
    public ResponseEntity<GroceryItemLabelResponse> createLabel(
            Principal principal,
            @Valid @RequestBody CreateGroceryItemLabelRequest request) {
        GroceryItemLabelResponse response = groceryItemLabelService.createLabel(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GroceryItemLabelResponse>> getLabels(Principal principal) {
        List<GroceryItemLabelResponse> response = groceryItemLabelService.getLabels(principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroceryItemLabelResponse> updateLabel(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGroceryItemLabelRequest request) {
        GroceryItemLabelResponse response = groceryItemLabelService.updateLabel(principal.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabel(
            Principal principal,
            @PathVariable UUID id) {
        groceryItemLabelService.deleteLabel(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
