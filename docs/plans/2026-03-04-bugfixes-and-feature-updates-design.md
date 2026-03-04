# Design: Bug Fixes & Feature Updates
**Date:** 2026-03-04

## Overview

Four changes: one auth bug fix, two feature removals/relaxations, one feature addition (separate item label pool).

---

## 1. Auth Bug Fix — Add `expiresAt` to AuthResponse

**Problem:** The frontend gets unauthorized after the access token expires and the refresh cycle fails. The root cause is likely that the frontend computes expiry from `expiresIn: 900` (seconds) incorrectly — either treating it as milliseconds, or suffering clock skew.

**Fix:** Add `expiresAt` (epoch milliseconds, `System.currentTimeMillis() + accessTokenExpiration`) to `AuthResponse`. The frontend can compare against `Date.now()` directly.

**Files changed:**
- `dto/response/AuthResponse.java` — add `expiresAt` field
- `service/AuthService.java` — pass `expiresAt` when constructing `AuthResponse`
- `service/JwtService.java` — add `generateExpiresAt()` helper or compute inline

---

## 2. Remove Grocery Auto-Archive

**Problem:** When all items in a grocery list are checked, the list auto-archives. Users want explicit control over archiving.

**Fix:** Remove the auto-archive block in `GroceryItemService.toggleChecked()`. Remove unused `existsByGroceryListAndCheckedFalse()` from `GroceryItemRepository`.

**Files changed:**
- `service/GroceryItemService.java` — remove auto-archive logic
- `repository/GroceryItemRepository.java` — remove `existsByGroceryListAndCheckedFalse()`

---

## 3. Optional Title for Notes and Grocery Lists

**Problem:** `@NotBlank` forces users to always provide a title, which is unnecessarily restrictive.

**Fix:** Remove `@NotBlank` from title in all four request DTOs. Change entity column to nullable. Manual DB migration required.

**Files changed:**
- `dto/request/CreateNoteRequest.java` — remove `@NotBlank`
- `dto/request/UpdateNoteRequest.java` — remove `@NotBlank`
- `dto/request/CreateGroceryListRequest.java` — remove `@NotBlank`
- `dto/request/UpdateGroceryListRequest.java` — remove `@NotBlank`
- `model/entity/Note.java` — `@Column(nullable = true)` on `title`
- `model/entity/GroceryList.java` — `@Column(nullable = true)` on `title`

**Manual DB migration:**
```sql
ALTER TABLE notes ALTER COLUMN title DROP NOT NULL;
ALTER TABLE grocery_lists ALTER COLUMN title DROP NOT NULL;
```

---

## 4. Separate Grocery Item Labels

**Problem:** `GroceryLabel` is a shared pool used by both grocery lists and grocery items. Item labels should be their own user-level pool independent of list labels.

**Design:**
- New `GroceryItemLabel` entity (table: `grocery_item_labels`) with `id, name, user, createdAt`
- Unique constraint: `(name, user_id)`
- `GroceryItem.labels` changes from `Set<GroceryLabel>` to `Set<GroceryItemLabel>`
- Join table `grocery_item_label_mappings` column `grocery_label_id` → `grocery_item_label_id`
- `GroceryLabel` loses `groceryItems` ManyToMany
- `GroceryLabelService.deleteLabel()` no longer needs to clean up items

**New endpoints at `/api/grocery-item-labels`:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/grocery-item-labels` | Create item label |
| GET | `/api/grocery-item-labels` | List user's item labels |
| PUT | `/api/grocery-item-labels/{id}` | Update item label name |
| DELETE | `/api/grocery-item-labels/{id}` | Delete item label (cleans up from items) |

**New files:**
- `model/entity/GroceryItemLabel.java`
- `repository/GroceryItemLabelRepository.java`
- `dto/request/CreateGroceryItemLabelRequest.java`
- `dto/request/UpdateGroceryItemLabelRequest.java`
- `dto/response/GroceryItemLabelResponse.java`
- `exception/DuplicateGroceryItemLabelException.java`
- `service/GroceryItemLabelService.java`
- `controller/GroceryItemLabelController.java`

**Modified files:**
- `model/entity/GroceryItemLabel.java` — new entity
- `model/entity/GroceryLabel.java` — remove `groceryItems` relationship
- `model/entity/GroceryItem.java` — change labels to `Set<GroceryItemLabel>`
- `service/GroceryItemService.java` — use `GroceryItemLabelRepository`
- `service/GroceryLabelService.java` — remove item cleanup from `deleteLabel()`
- `exception/GlobalExceptionHandler.java` — handle `DuplicateGroceryItemLabelException`
- `dto/response/GroceryItemResponse.java` — use `GroceryItemLabelResponse`

**Manual DB migration:**
```sql
-- Rename join table column (or recreate the table)
ALTER TABLE grocery_item_label_mappings
  RENAME COLUMN grocery_label_id TO grocery_item_label_id;

-- Add new table and FK
CREATE TABLE grocery_item_labels (
  id UUID PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  user_id UUID NOT NULL REFERENCES users(id),
  created_at TIMESTAMP NOT NULL,
  UNIQUE (name, user_id)
);

ALTER TABLE grocery_item_label_mappings
  ADD CONSTRAINT fk_grocery_item_label
  FOREIGN KEY (grocery_item_label_id) REFERENCES grocery_item_labels(id);
```
