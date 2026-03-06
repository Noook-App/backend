# Grocery Item Labels Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Give grocery items their own independent label pool, separate from grocery list labels.

**Architecture:** Create a new `GroceryItemLabel` entity that mirrors `GroceryLabel` exactly. Wire it into `GroceryItem`, add a full CRUD stack (repo → service → controller), expose it at `/api/grocery-lists/items/labels`, and clean up the now-irrelevant back-reference on `GroceryLabel`.

**Tech Stack:** Spring Boot 4.0.3, Jakarta Persistence, Spring Data JPA, Jakarta Validation, H2 (tests).

---

### Task 1: `DuplicateGroceryItemLabelException`

**Files:**
- Create: `src/main/java/com/personalspace/api/exception/DuplicateGroceryItemLabelException.java`
- Modify: `src/main/java/com/personalspace/api/exception/GlobalExceptionHandler.java`

**Step 1: Create the exception**

```java
package com.personalspace.api.exception;

public class DuplicateGroceryItemLabelException extends RuntimeException {
    public DuplicateGroceryItemLabelException(String message) {
        super(message);
    }
}
```

**Step 2: Register it in `GlobalExceptionHandler`**

Add after the `handleDuplicateGroceryLabel` handler (line 57):

```java
@ExceptionHandler(DuplicateGroceryItemLabelException.class)
public ResponseEntity<ApiErrorResponse> handleDuplicateGroceryItemLabel(DuplicateGroceryItemLabelException ex) {
    ApiErrorResponse response = new ApiErrorResponse(
            HttpStatus.CONFLICT.value(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
}
```

**Step 3: Compile check**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add src/main/java/com/personalspace/api/exception/DuplicateGroceryItemLabelException.java \
        src/main/java/com/personalspace/api/exception/GlobalExceptionHandler.java
git commit -m "feat: add DuplicateGroceryItemLabelException"
```

---

### Task 2: `GroceryItemLabel` entity

**Files:**
- Create: `src/main/java/com/personalspace/api/model/entity/GroceryItemLabel.java`

**Step 1: Create the entity**

```java
package com.personalspace.api.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "grocery_item_labels", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "user_id"})
})
public class GroceryItemLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToMany(mappedBy = "labels")
    private Set<GroceryItem> groceryItems = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public GroceryItemLabel() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Instant getCreatedAt() { return createdAt; }

    public Set<GroceryItem> getGroceryItems() { return groceryItems; }
    public void setGroceryItems(Set<GroceryItem> groceryItems) { this.groceryItems = groceryItems; }
}
```

**Step 2: Compile check**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add src/main/java/com/personalspace/api/model/entity/GroceryItemLabel.java
git commit -m "feat: add GroceryItemLabel entity"
```

---

### Task 3: Update `GroceryItem` to use `GroceryItemLabel`

**Files:**
- Modify: `src/main/java/com/personalspace/api/model/entity/GroceryItem.java`

**Step 1: Replace the `labels` field**

In `GroceryItem.java`, replace the existing import and field:

Old imports to remove:
```java
import com.personalspace.api.model.entity.GroceryLabel;
```

New import to add:
```java
import com.personalspace.api.model.entity.GroceryItemLabel;
```

Old field (lines 29–35):
```java
@ManyToMany
@JoinTable(
        name = "grocery_item_label_mappings",
        joinColumns = @JoinColumn(name = "grocery_item_id"),
        inverseJoinColumns = @JoinColumn(name = "grocery_label_id")
)
private Set<GroceryLabel> labels = new HashSet<>();
```

New field:
```java
@ManyToMany
@JoinTable(
        name = "grocery_item_label_mappings",
        joinColumns = @JoinColumn(name = "grocery_item_id"),
        inverseJoinColumns = @JoinColumn(name = "grocery_item_label_id")
)
private Set<GroceryItemLabel> labels = new HashSet<>();
```

Also update the getter/setter types:

Old:
```java
public Set<GroceryLabel> getLabels() { return labels; }
public void setLabels(Set<GroceryLabel> labels) { this.labels = labels; }
```

New:
```java
public Set<GroceryItemLabel> getLabels() { return labels; }
public void setLabels(Set<GroceryItemLabel> labels) { this.labels = labels; }
```

**Step 2: Compile check**

```bash
./mvnw compile -q
```
Expected: COMPILE ERROR — `GroceryItemService` still references `GroceryLabel`. That's expected; fix it in the next task.

**Step 3: Commit what compiles**

Skip commit until Task 4 fixes the compile errors.

---

### Task 4: Update `GroceryItemService` to use `GroceryItemLabelRepository`

First create the repository, then fix the service.

**Files:**
- Create: `src/main/java/com/personalspace/api/repository/GroceryItemLabelRepository.java`
- Modify: `src/main/java/com/personalspace/api/service/GroceryItemService.java`

**Step 1: Create the repository**

```java
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
```

**Step 2: Update `GroceryItemService`**

Add `GroceryItemLabelRepository` as a dependency and update `resolveLabels`:

In the field declarations, add:
```java
private final GroceryItemLabelRepository groceryItemLabelRepository;
```

Update the constructor to accept and assign it:
```java
public GroceryItemService(GroceryItemRepository groceryItemRepository,
                          GroceryListRepository groceryListRepository,
                          GroceryItemLabelRepository groceryItemLabelRepository,
                          UserService userService) {
    this.groceryItemRepository = groceryItemRepository;
    this.groceryListRepository = groceryListRepository;
    this.groceryItemLabelRepository = groceryItemLabelRepository;
    this.userService = userService;
}
```

Remove the old `GroceryLabelRepository` field, constructor param, and import.

Replace the `resolveLabels` method:
```java
private Set<GroceryItemLabel> resolveLabels(List<UUID> labelIds, User user) {
    if (labelIds == null || labelIds.isEmpty()) {
        return new HashSet<>();
    }
    return labelIds.stream()
            .map(id -> groceryItemLabelRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + id)))
            .collect(Collectors.toSet());
}
```

Update the return type of `resolveLabels` call sites — `createItem` and `updateItem` call `item.setLabels(resolveLabels(...))`, which now returns `Set<GroceryItemLabel>`. No further changes needed there since `GroceryItem.setLabels` now accepts `Set<GroceryItemLabel>`.

Also update the import in `GroceryItemService`:
- Remove: `import com.personalspace.api.repository.GroceryLabelRepository;`
- Add: `import com.personalspace.api.repository.GroceryItemLabelRepository;`
- Remove: `import com.personalspace.api.model.entity.GroceryLabel;` (if present)
- Add: `import com.personalspace.api.model.entity.GroceryItemLabel;`

**Step 3: Compile check**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add src/main/java/com/personalspace/api/model/entity/GroceryItem.java \
        src/main/java/com/personalspace/api/repository/GroceryItemLabelRepository.java \
        src/main/java/com/personalspace/api/service/GroceryItemService.java
git commit -m "feat: wire GroceryItem to GroceryItemLabel"
```

---

### Task 5: Clean up `GroceryLabel` — remove item back-reference

**Files:**
- Modify: `src/main/java/com/personalspace/api/model/entity/GroceryLabel.java`
- Modify: `src/main/java/com/personalspace/api/service/GroceryLabelService.java`

**Step 1: Remove `groceryItems` from `GroceryLabel`**

In `GroceryLabel.java`, delete:
```java
@ManyToMany(mappedBy = "labels")
private Set<GroceryItem> groceryItems = new HashSet<>();
```
And the getter/setter:
```java
public Set<GroceryItem> getGroceryItems() { return groceryItems; }
public void setGroceryItems(Set<GroceryItem> groceryItems) { this.groceryItems = groceryItems; }
```
Also remove unused imports for `GroceryItem` if present.

**Step 2: Remove item cleanup from `GroceryLabelService.deleteLabel`**

In `GroceryLabelService.java`, in the `deleteLabel` method, remove this line:
```java
label.getGroceryItems().forEach(item -> item.getLabels().remove(label));
```

**Step 3: Compile check**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add src/main/java/com/personalspace/api/model/entity/GroceryLabel.java \
        src/main/java/com/personalspace/api/service/GroceryLabelService.java
git commit -m "feat: remove item back-reference from GroceryLabel"
```

---

### Task 6: DTOs for `GroceryItemLabel`

**Files:**
- Create: `src/main/java/com/personalspace/api/dto/response/GroceryItemLabelResponse.java`
- Create: `src/main/java/com/personalspace/api/dto/request/CreateGroceryItemLabelRequest.java`
- Create: `src/main/java/com/personalspace/api/dto/request/UpdateGroceryItemLabelRequest.java`

**Step 1: Create response DTO**

```java
package com.personalspace.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GroceryItemLabelResponse(
        UUID id,
        String name,
        Instant createdAt
) {}
```

**Step 2: Create request DTOs**

```java
package com.personalspace.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroceryItemLabelRequest(
        @NotBlank(message = "Label name is required")
        @Size(max = 50, message = "Label name must not exceed 50 characters")
        String name
) {}
```

```java
package com.personalspace.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateGroceryItemLabelRequest(
        @NotBlank(message = "Label name is required")
        @Size(max = 50, message = "Label name must not exceed 50 characters")
        String name
) {}
```

**Step 3: Update `GroceryItemService.toGroceryItemResponse`**

The existing method maps labels to `GroceryLabelResponse`. Update it to use `GroceryItemLabelResponse`:

In `GroceryItemService.java`, change:
```java
List<GroceryLabelResponse> labelResponses = item.getLabels().stream()
        .map(label -> new GroceryLabelResponse(label.getId(), label.getName(), label.getCreatedAt()))
        .sorted(Comparator.comparing(GroceryLabelResponse::name))
        .toList();
```
to:
```java
List<GroceryItemLabelResponse> labelResponses = item.getLabels().stream()
        .map(label -> new GroceryItemLabelResponse(label.getId(), label.getName(), label.getCreatedAt()))
        .sorted(Comparator.comparing(GroceryItemLabelResponse::name))
        .toList();
```

Also update `GroceryItemResponse` if it references `GroceryLabelResponse` for the labels field — check `src/main/java/com/personalspace/api/dto/response/GroceryItemResponse.java` and update the labels parameter type from `List<GroceryLabelResponse>` to `List<GroceryItemLabelResponse>`.

**Step 4: Compile check**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add src/main/java/com/personalspace/api/dto/response/GroceryItemLabelResponse.java \
        src/main/java/com/personalspace/api/dto/request/CreateGroceryItemLabelRequest.java \
        src/main/java/com/personalspace/api/dto/request/UpdateGroceryItemLabelRequest.java \
        src/main/java/com/personalspace/api/dto/response/GroceryItemResponse.java \
        src/main/java/com/personalspace/api/service/GroceryItemService.java
git commit -m "feat: add GroceryItemLabel DTOs and update response mapping"
```

---

### Task 7: `GroceryItemLabelService`

**Files:**
- Create: `src/main/java/com/personalspace/api/service/GroceryItemLabelService.java`

**Step 1: Create the service**

```java
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
```

**Step 2: Compile check**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add src/main/java/com/personalspace/api/service/GroceryItemLabelService.java
git commit -m "feat: add GroceryItemLabelService"
```

---

### Task 8: `GroceryItemLabelController`

**Files:**
- Create: `src/main/java/com/personalspace/api/controller/GroceryItemLabelController.java`

**Step 1: Create the controller**

```java
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
```

**Step 2: Full test suite**

```bash
./mvnw test
```
Expected: BUILD SUCCESS, all tests pass.

**Step 3: Commit**

```bash
git add src/main/java/com/personalspace/api/controller/GroceryItemLabelController.java
git commit -m "feat: add GroceryItemLabelController at /api/grocery-lists/items/labels"
```
