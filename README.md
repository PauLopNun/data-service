# Data Service

Spring Boot service that **owns the database** and exposes a REST API consumed by [product-audit-service-practice](https://github.com/PauLopNun/product-audit-service-practice) when running in `rest` mode.

## Role in the architecture

```
[HTTP Client]
      |
      ▼ :8080
┌─────────────────────┐        ┌─────────────────────┐
│   audit-service     │──REST──│    data-service      │
│  (business logic)   │        │  (owns persistence)  │
│  port 8080          │        │  port 8081           │
└─────────────────────┘        └──────────┬──────────┘
                                           │ JDBC
                                           ▼ :5433
                                      PostgreSQL 16
```

- **audit-service**: handles HTTP requests, business logic, and audit queries — delegates all data operations here via REST.
- **data-service** (this service): owns PostgreSQL, runs Liquibase migrations, seeds initial data, and exposes the full CRUD + audit API.

## Prerequisites

- Java 21+
- Rancher Desktop or Docker Desktop

## Running

```bash
./mvnw spring-boot:run
```

Spring Boot auto-starts the PostgreSQL container (`docker-compose.yml`, port `5433`).  
Liquibase applies all migrations and loads 40 products from CSV.  
On first run a `CommandLineRunner` populates 5 allergies and 30 users.

## Configuration

```yaml
# application.yaml
server:
  port: 8081

spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://127.0.0.1:5433/demo}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
```

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://127.0.0.1:5433/demo` | JDBC URL |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |

## API Endpoints

### Users

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/users` | List all users with allergies |
| `GET` | `/api/users?nameContains={name}` | Filter by partial name |
| `GET` | `/api/users/count` | Total count |
| `POST` | `/api/users` | Create user `{"name":"..."}` |
| `PUT` | `/api/users/{id}` | Update user `{"name":"..."}` |

### Allergies

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/allergies` | List all allergies |
| `GET` | `/api/allergies/count` | Total count |
| `POST` | `/api/allergies` | Create allergy `{"name":"...","severity":"..."}` |

### Products + Audit (Hibernate Envers)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/products/{id}` | Get product by id |
| `PUT` | `/api/products/{id}` | Update product fields |
| `GET` | `/api/products/{id}/audit/revisions` | List revision numbers |
| `GET` | `/api/products/{id}/audit/{revision}` | Product state at revision |

## Tech stack

| Technology | Role |
|------------|------|
| Spring Boot 4.0.4 | Application framework |
| Spring Data JPA | ORM / repositories |
| Hibernate Envers | Product audit history |
| Liquibase | Schema migrations |
| PostgreSQL 16 | Database |
| Docker Compose | Auto-managed container |
| Lombok | Boilerplate reduction |
