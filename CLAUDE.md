# Noook API

Spring Boot 4.0.3 REST API for user authentication, profile management, notes, labels, and grocery lists.
Java 21, Maven, PostgreSQL (prod), H2 (test).

## Tech Stack

- **Framework**: Spring Boot 4.0.3 (spring-boot-starter-webmvc, spring-boot-starter-data-jpa)
- **Database**: PostgreSQL (prod), H2 (test)
- **Auth**: JWT via JJWT 0.12.6, Spring Security (stateless sessions)
- **Validation**: Jakarta Validation (spring-boot-starter-validation)
- **Env Loading**: spring-dotenv 3.0.0 (`me.paulschwarz:spring-dotenv`)

## Project Structure

```
src/main/java/com/personalspace/api/
  controller/        REST controllers (@RestController, @RequestMapping)
  service/           Business logic (@Service, @Transactional)
  repository/        Spring Data JPA interfaces (JpaRepository)
  model/entity/      JPA entities (@Entity, lifecycle hooks)
  model/enums/       Enumerations (Role)
  dto/request/       Inbound DTOs (Java records, Jakarta validation)
  dto/response/      Outbound DTOs (Java records)
  security/          SecurityConfig, JwtAuthenticationFilter, CustomUserDetailsService
  exception/         @RestControllerAdvice handler + custom RuntimeException subclasses
```

## Build & Test Commands

```bash
mvn clean package          # Build + run all tests
mvn spring-boot:run        # Start dev server
mvn test                   # Run all tests
mvn test -Dtest=ClassName  # Run a single test class
```

## Configuration

### Environment Variables (.env)

| Variable      | Used For                         |
|---------------|----------------------------------|
| `DB_URI`      | `spring.datasource.url`          |
| `DB_NAME`     | `spring.datasource.username`     |
| `DB_PASSWORD` | `spring.datasource.password`     |
| `JWT_SECRET`  | HMAC signing key (min 32 chars)  |

- `application.properties` loads `.env` via `spring.config.import=optional:file:.env[.properties]`
- JWT access token TTL: 15 min (hardcoded); refresh token TTL: 7 days (hardcoded)

### Test Profile

- `src/test/resources/application-test.properties` overrides datasource to H2 in-memory
- Provides a hardcoded `jwt.secret` so tests don't need `.env`
- Uses `ddl-auto=create-drop`

## API Endpoints

### Auth (`/api/auth`) — public

| Method | Path              | Auth Required | Description            |
|--------|-------------------|---------------|------------------------|
| POST   | `/api/auth/signup` | No           | Register a new user    |
| POST   | `/api/auth/login`  | No           | Login, get tokens      |
| POST   | `/api/auth/refresh`| No           | Refresh access token   |
| POST   | `/api/auth/logout` | No           | Invalidate refresh token|

### Notes (`/api/notes`) — authenticated

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/notes` | Create note |
| GET | `/api/notes?archived=false&page=0&size=10` | List notes (paginated, pinned first) |
| GET | `/api/notes/{id}` | Get single note |
| PUT | `/api/notes/{id}` | Update note |
| DELETE | `/api/notes/{id}` | Hard delete note |
| GET | `/api/notes/search?q=...&archived=false&page=0&size=10` | Search notes by title/content |
| PATCH | `/api/notes/{id}/pin` | Toggle pin |
| PATCH | `/api/notes/{id}/archive` | Toggle archive |

### Note Labels (`/api/notes/labels`) — authenticated

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/notes/labels` | Create note label |
| GET | `/api/notes/labels` | List user's note labels |
| PUT | `/api/notes/labels/{id}` | Update note label name |
| DELETE | `/api/notes/labels/{id}` | Delete note label |

### User Profile (`/api/user`) — authenticated

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/user/me` | Get current user's profile |

### Grocery Lists (`/api/grocery-lists`) — authenticated

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/grocery-lists` | Create grocery list (supports inline items) |
| GET | `/api/grocery-lists?archived=false&page=0&size=10` | List grocery lists (paginated) |
| GET | `/api/grocery-lists/{id}` | Get single grocery list with items |
| PUT | `/api/grocery-lists/{id}` | Update grocery list title/labels |
| DELETE | `/api/grocery-lists/{id}` | Hard delete grocery list (cascades items) |
| GET | `/api/grocery-lists/search?q=...&archived=false&page=0&size=10` | Search grocery lists by title/item names |
| PATCH | `/api/grocery-lists/{id}/archive` | Toggle archive |

### Grocery Items (`/api/grocery-lists/{listId}/items`) — authenticated

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/grocery-lists/{listId}/items` | Add item to list |
| GET | `/api/grocery-lists/{listId}/items` | List items (unchecked first) |
| PUT | `/api/grocery-lists/{listId}/items/{itemId}` | Update item |
| DELETE | `/api/grocery-lists/{listId}/items/{itemId}` | Delete item |
| PATCH | `/api/grocery-lists/{listId}/items/{itemId}/check` | Toggle checked (auto-archives list when all checked) |

### Grocery Labels (`/api/grocery-lists/labels`) — authenticated

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/grocery-lists/labels` | Create grocery label |
| GET | `/api/grocery-lists/labels` | List user's grocery labels |
| PUT | `/api/grocery-lists/labels/{id}` | Update grocery label name |
| DELETE | `/api/grocery-lists/labels/{id}` | Delete grocery label (cleans up from lists) |

### Grocery Item Labels (`/api/grocery-lists/items/labels`) — authenticated

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/grocery-lists/items/labels` | Create grocery item label |
| GET | `/api/grocery-lists/items/labels` | List user's grocery item labels |
| PUT | `/api/grocery-lists/items/labels/{id}` | Update grocery item label name |
| DELETE | `/api/grocery-lists/items/labels/{id}` | Delete grocery item label (cleans up from items) |

## Key Files

| File | Purpose |
|------|---------|
| `controller/AuthController.java` | Auth endpoints, delegates to AuthService |
| `controller/UserController.java` | User profile endpoint (`GET /api/user/me`) |
| `service/AuthService.java` | Signup, login, refresh, logout logic |
| `service/UserService.java` | User lookups by ID/email, profile retrieval |
| `service/JwtService.java` | Token generation, validation, claim extraction |
| `security/SecurityConfig.java` | Filter chain, CORS, session policy |
| `security/JwtAuthenticationFilter.java` | Extracts and validates Bearer tokens per request |
| `exception/GlobalExceptionHandler.java` | Maps exceptions to HTTP status codes |
| `model/entity/User.java` | User entity with @PrePersist/@PreUpdate timestamps |
| `model/entity/RefreshToken.java` | Refresh token entity, ManyToOne to User |
| `controller/NoteController.java` | Note CRUD, search, pin/archive endpoints |
| `controller/NoteLabelController.java` | Note label CRUD endpoints |
| `service/NoteService.java` | Note business logic, pagination, search |
| `service/NoteLabelService.java` | Note label business logic, duplicate checking |
| `model/entity/Note.java` | Note entity with ManyToMany labels, pin/archive |
| `model/entity/NoteLabel.java` | Note label entity with unique (name, user) constraint |
| `dto/response/PaginatedResponse.java` | Generic paginated response wrapper |
| `controller/GroceryListController.java` | Grocery list CRUD, search, archive endpoints |
| `controller/GroceryItemController.java` | Grocery item CRUD, check toggle endpoints |
| `controller/GroceryLabelController.java` | Grocery label CRUD endpoints |
| `controller/GroceryItemLabelController.java` | Grocery item label CRUD endpoints |
| `service/GroceryListService.java` | Grocery list business logic, inline item creation, search |
| `service/GroceryItemService.java` | Grocery item business logic, auto-archive on last check |
| `service/GroceryLabelService.java` | Grocery label business logic for lists |
| `service/GroceryItemLabelService.java` | Grocery item label business logic |
| `model/entity/GroceryList.java` | Grocery list entity with ManyToMany labels, OneToMany items |
| `model/entity/GroceryItem.java` | Grocery item entity with ManyToMany item labels, checked flag |
| `model/entity/GroceryLabel.java` | Label entity for grocery lists |
| `model/entity/GroceryItemLabel.java` | Label entity for grocery items |

## Additional Documentation

- [Architectural Patterns](.claude/docs/architectural_patterns.md) - Detailed patterns with file:line references
