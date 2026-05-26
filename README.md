# SANLY — Citizen Registry Service

**SANLY** (Туркм. _sanly_ — "digital") is a secure, production-grade e-governance platform prototype for Turkmenistan, inspired by Estonia's X-Road and Azerbaijan's ASAN service. Its mission is to digitize Soviet-era paper-based government services into a unified, connected digital platform.

The **Citizen Registry Service** is the foundation of the entire SANLY platform. Every other service (Medical, DMV, Tax, Business Registry, Civil Registry) will authenticate citizens against this registry.

---

## Architecture Overview

```
SANLY Platform (incremental build)
├── Citizen Registry Service  ◄─ YOU ARE HERE (Step 1)
├── TM-Login (Keycloak SSO)
├── SANLY Bridge (X-Road-inspired data exchange)
└── Institution Portals (Medical, DMV, Tax, Business, Civil)
```

### Internal Layering

```
Controller  →  Service  →  Repository  →  PostgreSQL
                 ↕
           AuditService (async)
```

---

## TM-NIN Format (Turkmenistan National ID Number)

A TM-NIN is an **11-digit string** generated automatically at registration.

| Positions | Meaning |
|-----------|---------|
| 1         | Gender + century code (1–6) |
| 2–3       | Birth year — last 2 digits (YY) |
| 4–5       | Birth month (MM) |
| 6–7       | Birth day (DD) |
| 8–10      | Daily sequence number within prefix (001–999) |
| 11        | Check digit |

**Gender/century codes:**

| Code | Gender | Century |
|------|--------|---------|
| 1 | Male   | 1800–1899 |
| 2 | Female | 1800–1899 |
| 3 | Male   | 1900–1999 |
| 4 | Female | 1900–1999 |
| 5 | Male   | 2000–2099 |
| 6 | Female | 2000–2099 |

**Check digit algorithm:**
```
weights_1 = [1,2,3,4,5,6,7,8,9,1]
check = (Σ digit[i] × weights_1[i]) mod 11
if check == 10:
    weights_2 = [3,4,5,6,7,8,9,1,2,3]
    check = (Σ digit[i] × weights_2[i]) mod 11
```

**Example:** Male, born 1990-03-15, 1st registration that day → `39003150011`

---

## Prerequisites

- Docker & Docker Compose (recommended), **or**
- JDK 17+, Maven 3.9+, PostgreSQL 16

---

## Quick Start (Docker)

```bash
# 1. Copy and edit environment variables
cp .env.example .env
# Fill in DB_PASSWORD, JWT_SECRET, ENCRYPTION_KEY, ADMIN_PASSWORD, INSTITUTION_PASSWORD

# 2. Start all services
docker-compose up --build

# Application: http://localhost:8080
# Swagger UI:  http://localhost:8080/swagger-ui.html
# pgAdmin:     http://localhost:5050
```

---

## Running Locally (without Docker)

```bash
# 1. Start PostgreSQL and create the database
createdb sanly_registry

# 2. Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/sanly_registry
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-jwt-secret-at-least-32-characters-long
export ENCRYPTION_KEY=your-exactly-32-chars-key!!!!!!!!
export ADMIN_PASSWORD=Admin@Sanly2024!
export INSTITUTION_PASSWORD=Inst@Sanly2024!

# 3. Build and run
mvn spring-boot:run
```

Flyway migrations run automatically on startup and create all tables.

---

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `DB_URL` | No | JDBC URL (default: `jdbc:postgresql://localhost:5432/sanly_registry`) |
| `DB_USERNAME` | No | Database username (default: `sanly_user`) |
| `DB_PASSWORD` | **Yes** | Database password |
| `JWT_SECRET` | **Yes** | JWT signing key (≥ 32 chars) |
| `ENCRYPTION_KEY` | **Yes** | AES-256 field encryption key (exactly 32 chars) |
| `JWT_EXPIRATION_MS` | No | Token TTL in ms (default: `86400000` = 24h) |
| `ADMIN_USERNAME` | No | Initial admin username (default: `admin`) |
| `ADMIN_PASSWORD` | **Yes** | Initial admin password |
| `INSTITUTION_USERNAME` | No | Initial institution username (default: `institution`) |
| `INSTITUTION_PASSWORD` | **Yes** | Initial institution password |

---

## Security

| Mechanism | Details |
|-----------|---------|
| Authentication | Stateless JWT (HMAC-SHA256), 24h expiry |
| Password hashing | BCrypt, cost factor 12 |
| Field encryption | AES-256-GCM (street, placeOfBirth, photoUrl) |
| Authorization | Method-level `@PreAuthorize` with role checks |
| Rate limiting | 100 req / min per IP (in-memory, Bucket4j) |
| Audit log | Every API call is recorded asynchronously |

### Roles

| Role | Permissions |
|------|-------------|
| `ROLE_ADMIN` | Full access — all endpoints |
| `ROLE_INSTITUTION` | Read + verify: GET by NIN, verify, search |
| `ROLE_CITIZEN` | Own record only: GET + verify their own NIN |

### Encryption note
`firstName`, `lastName`, and `dateOfBirth` are stored **unencrypted** to support database-side search. In production, use deterministic encryption or a separate search index (e.g., Elasticsearch) for these fields, and store the sensitive versions encrypted. `street`, `placeOfBirth`, and `photoUrl` use AES-256-GCM randomized encryption.

---

## API Endpoints

All endpoints require a `Authorization: Bearer <token>` header except `/api/v1/auth/login`.

### Authentication

#### POST `/api/v1/auth/login`
```json
// Request
{
  "username": "admin",
  "password": "Admin@Sanly2024!"
}

// Response 200
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "username": "admin",
    "roles": ["ROLE_ADMIN"]
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Citizens

#### POST `/api/v1/citizens` — Register a new citizen
**Roles:** ADMIN

```json
// Request
{
  "firstName": "Merdan",
  "lastName": "Ataýew",
  "middleName": "Gurbanowiç",
  "dateOfBirth": "1990-03-15",
  "gender": "MALE",
  "placeOfBirth": "Ashgabat",
  "address": {
    "street": "Bitarap Türkmenistan şaýoly 5",
    "city": "Ashgabat",
    "region": "Ahal"
  },
  "fatherId": null,
  "motherId": null,
  "spouseId": null
}

// Response 201
{
  "success": true,
  "message": "Citizen registered. National ID: 39003150011",
  "data": {
    "nationalId": "39003150011",
    "firstName": "Merdan",
    "lastName": "Ataýew",
    "dateOfBirth": "1990-03-15",
    "gender": "MALE",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:31:00",
    "updatedAt": "2024-01-15T10:31:00"
  }
}
```

---

#### GET `/api/v1/citizens/{nationalId}` — Get citizen by National ID
**Roles:** ADMIN, INSTITUTION, CITIZEN (own record only)

```
GET /api/v1/citizens/39003150011

Response 200: Full CitizenResponse object
Response 404: { "status": 404, "error": "Not Found", "message": "Citizen not found..." }
```

---

#### GET `/api/v1/citizens/{nationalId}/verify` — Verify citizen status
**Roles:** ADMIN, INSTITUTION, CITIZEN

```json
// Response 200 (citizen found and active)
{
  "success": true,
  "data": {
    "nationalId": "39003150011",
    "exists": true,
    "active": true,
    "status": "ACTIVE",
    "fullName": "Ataýew Merdan Gurbanowiç"
  }
}

// Response 200 (citizen not found — never 404 for verify)
{
  "success": true,
  "data": {
    "nationalId": "39003150011",
    "exists": false,
    "active": false,
    "status": null
  }
}
```

---

#### PUT `/api/v1/citizens/{nationalId}` — Update citizen record
**Roles:** ADMIN

```json
// Request (all fields optional — only provided fields are updated)
{
  "firstName": "Merdan",
  "address": {
    "city": "Mary",
    "region": "Mary"
  }
}
```

---

#### PATCH `/api/v1/citizens/{nationalId}/status` — Update status
**Roles:** ADMIN

```json
// Request
{
  "status": "SUSPENDED",
  "reason": "Administrative review"
}

// Allowed values: ACTIVE, DECEASED, SUSPENDED
```

---

#### GET `/api/v1/citizens/search` — Search citizens
**Roles:** ADMIN, INSTITUTION

```
GET /api/v1/citizens/search?name=Merdan&region=Ahal&page=0&size=20
GET /api/v1/citizens/search?dob=1990-03-15
GET /api/v1/citizens/search?name=Atayew&dob=1990-03-15&region=Ahal

// Response 200
{
  "success": true,
  "data": {
    "content": [ /* CitizenResponse objects */ ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

## Error Response Format

All errors return a consistent JSON structure:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields failed validation",
  "fieldErrors": {
    "firstName": "First name is required",
    "dateOfBirth": "Date of birth must be in the past"
  },
  "path": "/api/v1/citizens",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Project Structure

```
com.sanly.registry
├── RegistryApplication.java        Spring Boot entry point
├── config/
│   ├── SecurityConfig.java         Spring Security + JWT filter chain
│   ├── JwtTokenProvider.java       Token generation & validation
│   ├── JwtAuthenticationFilter.java  Per-request JWT extraction
│   ├── AuthEntryPoint.java         401 JSON response
│   ├── RateLimitingFilter.java     Bucket4j per-IP rate limiter
│   ├── EncryptionService.java      AES-256-GCM encrypt/decrypt
│   ├── EncryptedStringConverter.java  JPA AttributeConverter
│   ├── UserDetailsImpl.java        Spring Security principal
│   └── DataInitializer.java        Seed admin/institution users
├── controller/
│   ├── CitizenController.java      REST endpoints for citizens
│   └── AuthController.java         Login endpoint
├── service/
│   ├── CitizenService.java         Interface
│   ├── CitizenServiceImpl.java     Business logic + NIN generation
│   ├── UserDetailsServiceImpl.java Spring Security user loading
│   └── SecurityService.java        SpEL RBAC helper (@securityService)
├── repository/
│   ├── CitizenRepository.java
│   ├── AuditLogRepository.java
│   ├── UserRepository.java
│   └── NinSequenceRepository.java  Pessimistic-lock sequence counter
├── entity/
│   ├── Citizen.java
│   ├── Address.java               @Embeddable
│   ├── AuditLog.java
│   ├── User.java
│   ├── NinSequence.java
│   ├── Gender.java                 MALE | FEMALE
│   └── CitizenStatus.java          ACTIVE | DECEASED | SUSPENDED
├── dto/
│   ├── request/                    Input DTOs with Bean Validation
│   └── response/                   Output DTOs (entity never exposed)
├── exception/
│   ├── CitizenNotFoundException.java
│   ├── InvalidNationalIdException.java
│   ├── RelatedCitizenNotFoundException.java
│   ├── RegistrationLimitExceededException.java
│   └── GlobalExceptionHandler.java  @RestControllerAdvice
├── util/
│   ├── NationalIdGenerator.java    TM-NIN generation
│   └── NationalIdValidator.java    TM-NIN structural validation
└── audit/
    ├── AuditService.java
    └── AuditServiceImpl.java        Async audit log persistence
```

---

## Database Schema

| Table | Purpose |
|-------|---------|
| `citizens` | Core citizen records |
| `audit_logs` | Immutable access log (who/what/when) |
| `users` | Platform user accounts |
| `user_roles` | User-role join table |
| `nin_sequences` | Per-prefix sequence counters for NIN generation |

Migrations are managed by Flyway (`V1__`, `V2__`, `V3__`).

---

## Testing

The two most critical services — **citizen-registry** (this module) and **sanly-bridge** — have comprehensive test suites covering unit tests, controller slice tests, and full end-to-end integration tests.

### Running Tests

```bash
# Run all tests (unit + integration) and generate JaCoCo coverage report
mvn test

# Run tests for sanly-bridge separately
cd ../sanly-bridge && mvn test
```

### Requirements

- **Testcontainers** — integration tests spin up a real PostgreSQL 15 container automatically. Docker must be running on the host.
- **Docker** — required for Testcontainers. No manual database setup is needed for tests.

### Test Coverage

JaCoCo generates an HTML coverage report after `mvn test`:

```
citizen-registry:  target/site/jacoco/index.html
sanly-bridge:      ../sanly-bridge/target/site/jacoco/index.html
```

DTOs, entities, and application bootstrap classes are excluded from coverage metrics.

### Test Structure

#### citizen-registry

| Test class | Type | What it covers |
|------------|------|----------------|
| `NationalIdGeneratorTest` | Unit | TM-NIN format, check-digit algorithm, boundary sequences, validator |
| `CitizenServiceTest` | Unit | register, getByNationalId, verify, update, search — all paths |
| `CitizenControllerTest` | Controller slice (`@WebMvcTest`) | HTTP status codes, role-based access, request/response shapes |
| `CitizenRegistryIntegrationTest` | Integration (Testcontainers) | Full register→retrieve→verify→status-change flow against a real DB |

#### sanly-bridge

| Test class | Type | What it covers |
|------------|------|----------------|
| `ExchangePermissionServiceTest` | Unit | grant, revoke, idempotency, unknown-institution errors |
| `AnomalyDetectionServiceTest` | Unit | All 6 anomaly rules, deduplication, PUBLISH no-op, notifications |
| `ExchangeControllerTest` | Controller slice (`@WebMvcTest`) | Institution header auth, query/publish/getData endpoints |
| `BridgeIntegrationTest` | Integration (Testcontainers) | Full publish→query→revoke→DENIED scenario against a real DB |

### Test Profiles

Both services use `src/test/resources/application-test.yml` (activated via `@ActiveProfiles("test")`) which provides:
- A short JWT secret for fast token generation
- Generous rate-limit buckets that never fire during tests
- Placeholder URLs for external services (mocked with `@MockBean`)

---

## What's Next (SANLY Step 2+)

- **TM-Login** — Keycloak-based SSO for all portals
- **SANLY Bridge** — X-Road-inspired secure data exchange layer
- **Institution Portals** — Medical records, DMV, Tax, Business Registry, Civil Registry
