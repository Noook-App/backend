package com.personalspace.api.controller;

import com.personalspace.api.dto.request.CreateGroceryItemRequest;
import com.personalspace.api.dto.request.UpdateGroceryItemRequest;
import com.personalspace.api.dto.response.GroceryItemResponse;
import com.personalspace.api.service.GroceryItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/grocery-lists/{listId}/items")
public class GroceryItemController {

    private final GroceryItemService groceryItemService;

    public GroceryItemController(GroceryItemService groceryItemService) {
        this.groceryItemService = groceryItemService;
    }

    @PostMapping
    public ResponseEntity<GroceryItemResponse> createItem(
            Principal principal,
            @PathVariable UUID listId,
            @Valid @RequestBody CreateGroceryItemRequest request) {
        GroceryItemResponse response = groceryItemService.createItem(principal.getName(), listId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GroceryItemResponse>> getItems(
            Principal principal,
            @PathVariable UUID listId) {
        List<GroceryItemResponse> response = groceryItemService.getItems(principal.getName(), listId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<GroceryItemResponse> updateItem(
            Principal principal,
            @PathVariable UUID listId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateGroceryItemRequest request) {
        GroceryItemResponse response = groceryItemService.updateItem(principal.getName(), listId, itemId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            Principal principal,
            @PathVariable UUID listId,
            @PathVariable UUID itemId) {
        groceryItemService.deleteItem(principal.getName(), listId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{itemId}/check")
    public ResponseEntity<GroceryItemResponse> toggleChecked(
            Principal principal,
            @PathVariable UUID listId,
            @PathVariable UUID itemId) {
        GroceryItemResponse response = groceryItemService.toggleChecked(principal.getName(), listId, itemId);
        return ResponseEntity.ok(response);
    }
}
