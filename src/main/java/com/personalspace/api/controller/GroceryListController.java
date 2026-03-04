package com.personalspace.api.controller;

import com.personalspace.api.dto.request.CreateGroceryListRequest;
import com.personalspace.api.dto.request.UpdateGroceryListRequest;
import com.personalspace.api.dto.response.GroceryListResponse;
import com.personalspace.api.dto.response.PaginatedResponse;
import com.personalspace.api.service.GroceryListService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/grocery-lists")
public class GroceryListController {

    private final GroceryListService groceryListService;

    public GroceryListController(GroceryListService groceryListService) {
        this.groceryListService = groceryListService;
    }

    @PostMapping
    public ResponseEntity<GroceryListResponse> createGroceryList(
            Principal principal,
            @Valid @RequestBody CreateGroceryListRequest request) {
        GroceryListResponse response = groceryListService.createGroceryList(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<GroceryListResponse>> getGroceryLists(
            Principal principal,
            @RequestParam(defaultValue = "false") boolean archived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponse<GroceryListResponse> response = groceryListService.getGroceryLists(principal.getName(), archived, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroceryListResponse> getGroceryList(
            Principal principal,
            @PathVariable UUID id) {
        GroceryListResponse response = groceryListService.getGroceryList(principal.getName(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroceryListResponse> updateGroceryList(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGroceryListRequest request) {
        GroceryListResponse response = groceryListService.updateGroceryList(principal.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroceryList(
            Principal principal,
            @PathVariable UUID id) {
        groceryListService.deleteGroceryList(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<GroceryListResponse> toggleArchive(
            Principal principal,
            @PathVariable UUID id) {
        GroceryListResponse response = groceryListService.toggleArchive(principal.getName(), id);
        return ResponseEntity.ok(response);
    }
}
