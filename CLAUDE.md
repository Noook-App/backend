# PersonaSpace API

Spring Boot 4.0.3 REST API for user authentication, profile management, notes, and labels.
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

## Key Files

| File | Purpose |
|------|---------|
| `controller/AuthController.java` | Auth endpoints, delegates to AuthService |
| `service/AuthService.java` | Signup, login, refresh, logout logic |
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

## Additional Documentation

- [Architectural Patterns](.claude/docs/architectural_patterns.md) - Detailed patterns with file:line references
