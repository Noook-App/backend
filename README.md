# PersonaSpace API

REST API for the PersonaSpace application, handling user authentication with JWT-based stateless security.

Built with Java 21, Spring Boot 4.0.3, and PostgreSQL.

## Features

- User registration with email/password
- Login with JWT access + refresh token issuance
- Token refresh with automatic rotation
- Logout via refresh token invalidation
- Notes CRUD with pagination (pinned first, then by recency)
- Full-text search on note title and content (case-insensitive)
- Pin and archive (soft delete) toggle for notes
- Labels with many-to-many relationship to notes
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

All endpoints are under `/api/auth` and accept/return JSON.

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

## Labels API (authenticated — requires Bearer token)

### POST `/api/labels`

**Request body:**
```json
{ "name": "Work" }
```

**Response (201):**
```json
{ "id": "label-uuid", "name": "Work", "createdAt": "..." }
```

### GET `/api/labels`

Returns all labels for the authenticated user, sorted alphabetically.

### PUT `/api/labels/{id}`

Update label name. Returns 409 if duplicate name.

### DELETE `/api/labels/{id}`

Delete a label (removes it from all associated notes). Returns 204.

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
| 409 | Email already registered / Duplicate label name |

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
