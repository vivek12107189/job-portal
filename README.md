# Job Portal Backend

Spring Boot backend for a Job Portal application with JWT auth, role-based access, Flyway migrations, and PostgreSQL.

## Tech Stack
- Java 21
- Spring Boot 4
- Spring Security (JWT)
- Spring Data JPA
- Flyway
- PostgreSQL
- OpenAPI/Swagger

## Local Run
1. Ensure Java 21 is active.
2. Create/update `.env` in project root:

```env
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/job_portal
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_real_postgres_password
JWT_SECRET=replace-with-at-least-32-char-secret
JWT_EXPIRATION_MS=3600000
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000
```

3. Start app:

```bash
./mvnw spring-boot:run
```

## API Docs
When app is running:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Run with Docker
Start app + database:

```bash
docker compose up --build
```

Stop services:

```bash
docker compose down
```

The app will be available at `http://localhost:8080`.

## Testing
```bash
./mvnw test
```
