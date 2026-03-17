# Noook API

REST API for the Noook application, handling user authentication with JWT-based stateless security.

Built with Java 21, Spring Boot 4.0.3, and PostgreSQL.

## Features

- User registration with email/password
- Login with JWT access + refresh token issuance
- Token refresh with automatic rotation
- Logout via refresh token invalidation
- User profile endpoint
- Notes CRUD with pagination (pinned first, then by recency)
- Full-text search on note title and content (case-insensitive)
- Pin and archive (soft delete) toggle for notes
- Note labels with many-to-many relationship to notes
- Grocery list CRUD with pagination and search
- Grocery list archive toggle (auto-archives when all items checked)
- Grocery items with check/uncheck toggle
- Inline item creation when creating grocery lists
- Grocery list labels and grocery item labels (separate label pools)
- Input validation with descriptive error messages
- BCrypt password hashing

## Prerequisites

- **Java 21** or later
- **Maven 3.9+**
- **PostgreSQL** database (e.g., Supabase, local instance, or Docker)

## Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd api
```

### 2. Set up environment variables

Copy the example env file and fill in your values:

```bash
cp .env.example .env
```

Edit `.env` with your database credentials and a JWT secret:

```
DB_URI=jdbc:postgresql://<host>:<port>/<database>
DB_NAME=<database_username>
DB_PASSWORD=<database_password>
JWT_SECRET=<a_random_string_at_least_32_characters>
```

> **Note:** `DB_NAME` is used for the database username (not the database name). `DB_URI` is the full JDBC connection URL.

### 3. Run the application

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Hibernate will auto-create the database tables on first run.

### 4. Verify it's working

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name": "Test User", "email": "test@example.com", "password": "password123"}'
```

You should get back a JSON response with `accessToken`, `refreshToken`, `tokenType`, and `expiresIn`.

## API Endpoints

Auth endpoints are under `/api/auth` and accept/return JSON. All other endpoints require a Bearer token.

### POST `/api/auth/signup`

Register a new user.

**Request body:**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "securepass"
}
```

**Response (201):**
```json
{
  "accessToken": "eyJhbG...",
  "refreshToken": "550e8400-e29b-...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```

### POST `/api/auth/login`

Authenticate an existing user.

**Request body:**
```json
{
  "email": "jane@example.com",
  "password": "securepass"
}
```

**Response (200):** Same shape as signup.

### POST `/api/auth/refresh`

Get a new access token using a refresh token. The old refresh token is invalidated and a new one is returned (token rotation).

**Request body:**
```json
{
  "refreshToken": "550e8400-e29b-..."
}
```

**Response (200):** Same shape as signup.

### POST `/api/auth/logout`

Invalidate a refresh token.

**Request body:**
```json
{
  "refreshToken": "550e8400-e29b-..."
}
```

**Response:** 204 No Content

## Notes API (authenticated — requires Bearer token)

### POST `/api/notes`

Create a new note.

**Request body:**
```json
{
  "title": "My Note",
  "content": "<p>HTML content</p>",
  "pinned": false,
  "labelIds": ["uuid-of-label"]
}
```

**Response (201):**
```json
{
  "id": "note-uuid",
  "title": "My Note",
  "content": "<p>HTML content</p>",
  "pinned": false,
  "archived": false,
  "labels": [{"id": "label-uuid", "name": "Work", "createdAt": "..."}],
  "createdAt": "...",
  "updatedAt": "..."
}
```

### GET `/api/notes?archived=false&page=0&size=10`

List notes with pagination. Pinned notes appear first, then sorted by recency.

### GET `/api/notes/{id}`

Get a single note by ID. Returns 404 if not found or not owned by user.

### PUT `/api/notes/{id}`

Update a note. Request body same shape as create, with optional `archived` field.

### DELETE `/api/notes/{id}`

Hard delete a note. Returns 204 No Content.

### GET `/api/notes/search?q=keyword&archived=false&page=0&size=10`

Search notes by title or content (case-insensitive). Blank query returns all notes.

### PATCH `/api/notes/{id}/pin`

Toggle the pinned status of a note.

### PATCH `/api/notes/{id}/archive`

Toggle the archived status of a note.

## Note Labels API (authenticated — requires Bearer token)

### POST `/api/notes/labels`

**Request body:**
```json
{ "name": "Work" }
```

**Response (201):**
```json
{ "id": "label-uuid", "name": "Work", "createdAt": "..." }
```

### GET `/api/notes/labels`

Returns all note labels for the authenticated user, sorted alphabetically.

### PUT `/api/notes/labels/{id}`

Update label name. Returns 409 if duplicate name.

### DELETE `/api/notes/labels/{id}`

Delete a label (removes it from all associated notes). Returns 204.

## User Profile API (authenticated — requires Bearer token)

### GET `/api/user/me`

Returns the current user's profile (`name`, `email`).

## Grocery Lists API (authenticated — requires Bearer token)

### POST `/api/grocery-lists`

Create a grocery list with optional inline items and labels.

**Request body:**
```json
{
  "title": "Weekly Shopping",
  "items": [{ "name": "Milk", "quantity": "1L" }],
  "labelIds": ["label-uuid"]
}
```

### GET `/api/grocery-lists?archived=false&page=0&size=10`

List grocery lists with pagination.

### GET `/api/grocery-lists/{id}`

Get a single grocery list with all items.

### PUT `/api/grocery-lists/{id}`

Update grocery list title and labels.

### DELETE `/api/grocery-lists/{id}`

Hard delete a grocery list (cascades to items). Returns 204.

### GET `/api/grocery-lists/search?q=keyword&archived=false&page=0&size=10`

Search grocery lists by title or item names (case-insensitive).

### PATCH `/api/grocery-lists/{id}/archive`

Toggle archived status.

## Grocery Items API (authenticated — requires Bearer token)

### POST `/api/grocery-lists/{listId}/items`

Add an item to a grocery list.

### GET `/api/grocery-lists/{listId}/items`

List items (unchecked first, then by creation date).

### PUT `/api/grocery-lists/{listId}/items/{itemId}`

Update item name, quantity, and labels.

### DELETE `/api/grocery-lists/{listId}/items/{itemId}`

Delete an item. Returns 204.

### PATCH `/api/grocery-lists/{listId}/items/{itemId}/check`

Toggle checked status. Auto-archives the list when all items are checked.

## Grocery Labels API (authenticated — requires Bearer token)

### POST `/api/grocery-lists/labels`

Create a grocery list label. Returns 409 on duplicate name.

### GET `/api/grocery-lists/labels`

List user's grocery labels alphabetically.

### PUT `/api/grocery-lists/labels/{id}`

Update label name.

### DELETE `/api/grocery-lists/labels/{id}`

Delete label (removes from all grocery lists). Returns 204.

## Grocery Item Labels API (authenticated — requires Bearer token)

### POST `/api/grocery-lists/items/labels`

Create a grocery item label. Returns 409 on duplicate name.

### GET `/api/grocery-lists/items/labels`

List user's grocery item labels alphabetically.

### PUT `/api/grocery-lists/items/labels/{id}`

Update label name.

### DELETE `/api/grocery-lists/items/labels/{id}`

Delete label (removes from all grocery items). Returns 204.

### Error responses

All errors return a consistent format:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email must be valid",
    "password": "Password must be at least 8 characters"
  },
  "timestamp": "2026-03-02T10:30:00Z"
}
```

| Status | When |
|--------|------|
| 400 | Validation failure (missing/invalid fields) |
| 401 | Wrong password or invalid/expired refresh token |
| 404 | Resource not found |
| 409 | Email already registered / Duplicate label name (note, grocery, or item) |

## Running Tests

```bash
mvn test
```

Tests use an in-memory H2 database, so no PostgreSQL setup is needed. To run a specific test class:

```bash
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=JwtServiceTest
mvn test -Dtest=UserRepositoryTest
```

## Project Structure

```
src/main/java/com/personalspace/api/
  controller/       HTTP endpoints
  service/          Business logic
  repository/       Database access (Spring Data JPA)
  model/entity/     JPA entities (database tables)
  model/enums/      Enumerations
  dto/request/      Incoming request shapes
  dto/response/     Outgoing response shapes
  security/         JWT filter, Spring Security config
  exception/        Global error handler + custom exceptions
```

See [PROJECT_GUIDE.md](PROJECT_GUIDE.md) for a detailed walkthrough of every file and how the pieces fit together.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.3 |
| Database | PostgreSQL (prod), H2 (test) |
| ORM | Spring Data JPA / Hibernate |
| Auth | JWT (JJWT 0.12.6), Spring Security |
| Validation | Jakarta Bean Validation |
| Build | Maven |
| Env management | spring-dotenv |
