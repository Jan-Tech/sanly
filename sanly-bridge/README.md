# SANLY Bridge

**SANLY Bridge** is the secure inter-agency data exchange layer of the SANLY e-governance platform. It is inspired by Estonia's X-Road and Azerbaijan's ASAN Bridge.

Every government institution (Medical, DMV, Tax, Education, Police) communicates with every other institution **exclusively through SANLY Bridge**. Institutions never talk to each other's databases directly.

---

## How It Works

```
DMV Portal ──► SANLY Bridge ──► Validates DMV's API key
                             ──► Checks DMV has permission to query VISION_TEST
                             ──► Fetches INST_MEDICAL's published records
                             ──► Logs the exchange (who, what, when, result)
                             ──► Returns response to DMV
```

### Key Concepts

| Concept | Description |
|---------|-------------|
| **Institution** | A government agency registered in the bridge (e.g. `INST_MEDICAL`) |
| **API Key** | A 64-char random hex key issued once at registration — BCrypt-hashed in DB |
| **Publishable Types** | Data types an institution may push into the bridge |
| **Permission** | An explicit grant allowing Institution A to query data type D from Institution B |
| **Published Data** | Metadata record an institution publishes; raw PII stays in their own system |
| **Exchange Log** | Immutable record of every exchange — who asked, who answered, what, when, result |

### Data Types

| Enum | Description |
|------|-------------|
| `VISION_TEST` | Eye test result (relevant to DMV for license issuance) |
| `MEDICAL_CLEARANCE` | General medical fitness certificate |
| `CRIMINAL_RECORD` | Police criminal background check |
| `TAX_STATUS` | Tax compliance status |
| `EDUCATION_DIPLOMA` | Verified diploma record |
| `DRIVING_LICENSE` | Active license status |
| `PROPERTY_RECORD` | Property ownership registry |
| `SOCIAL_BENEFIT_STATUS` | Social welfare enrollment |

---

## Quick Start (Docker)

```bash
cp .env.example .env
# Fill in DB_PASSWORD, JWT_SECRET, ADMIN_PASSWORD

docker-compose up --build

# Application:  http://localhost:8081
# Swagger UI:   http://localhost:8081/swagger-ui.html
# pgAdmin:      http://localhost:5051
```

---

## Authentication

SANLY Bridge uses **two separate authentication mechanisms**:

### 1. Admin JWT (management endpoints)
```
POST /api/v1/auth/login
Authorization: Bearer <token>
```

### 2. Institution API Key (exchange endpoints)
```
X-Institution-Code: INST_MEDICAL
X-Institution-Key: sk_bridge_<64-hex-chars>
```

---

## Institution Lifecycle

```
1. Admin registers institution  → POST /api/v1/institutions
                                 ← Returns one-time API key
2. Admin grants permissions     → POST /api/v1/permissions
3. Institution publishes data   → POST /api/v1/exchange/publish
4. Another institution queries  → POST /api/v1/exchange/query
5. Admin audits all exchanges   → GET /api/v1/audit/exchanges
```

---

## API Endpoints

### Authentication

#### POST `/api/v1/auth/login`
```json
// Request
{ "username": "admin", "password": "Admin@Bridge2024!" }

// Response 200
{
  "success": true,
  "data": { "token": "eyJ...", "tokenType": "Bearer", "expiresIn": 86400000 }
}
```

---

### Institution Management  *(ROLE_ADMIN — Bearer JWT)*

#### POST `/api/v1/institutions` — Register institution
```json
// Request
{
  "institutionCode": "INST_MEDICAL",
  "name": "Ministry of Health",
  "description": "National health records authority",
  "publishableTypes": ["VISION_TEST", "MEDICAL_CLEARANCE"]
}

// Response 201 — API key shown ONCE
{
  "success": true,
  "message": "Institution registered. Store the API key securely — it will not be shown again.",
  "data": {
    "institutionCode": "INST_MEDICAL",
    "name": "Ministry of Health",
    "publishableTypes": ["VISION_TEST", "MEDICAL_CLEARANCE"],
    "status": "ACTIVE",
    "apiKey": "sk_bridge_a3f92e...8d41"
  }
}
```

#### GET `/api/v1/institutions` — List all institutions
#### GET `/api/v1/institutions/{institutionCode}` — Get institution details
#### PATCH `/api/v1/institutions/{institutionCode}/status` — Activate / suspend
```json
{ "status": "SUSPENDED", "reason": "Under investigation" }
```
#### POST `/api/v1/institutions/{institutionCode}/rotate-key` — Rotate API key
Old key is invalidated immediately. New key returned once.

---

### Permission Management  *(ROLE_ADMIN — Bearer JWT)*

#### POST `/api/v1/permissions` — Grant permission
```json
// Request: DMV can query eye test data from Medical
{
  "requestingCode": "INST_DMV",
  "targetCode":     "INST_MEDICAL",
  "dataType":       "VISION_TEST"
}

// Response 201
{
  "success": true,
  "data": {
    "id": 1,
    "requestingCode": "INST_DMV",
    "targetCode": "INST_MEDICAL",
    "dataType": "VISION_TEST",
    "grantedBy": "admin",
    "grantedAt": "2024-01-15T10:00:00",
    "active": true
  }
}
```

#### GET `/api/v1/permissions` — List all active permissions
#### DELETE `/api/v1/permissions/{id}` — Revoke permission (soft-delete, kept for audit)

---

### Data Exchange  *(X-Institution-Code + X-Institution-Key)*

#### POST `/api/v1/exchange/publish` — Publish a data record
```json
// Headers: X-Institution-Code: INST_MEDICAL  X-Institution-Key: sk_bridge_...

// Request
{
  "nationalId": "39003150011",
  "dataType": "VISION_TEST",
  "recordRef": "MED-2024-V-001",
  "summary": { "passed": true, "acuity": "20/20", "date": "2024-01-14" },
  "expiresAt": "2025-01-14T00:00:00"
}

// Response 201
{
  "success": true,
  "data": {
    "exchangeId": 1,
    "requestingCode": "INST_MEDICAL",
    "dataType": "VISION_TEST",
    "operationType": "PUBLISH",
    "result": "SUCCESS",
    "records": [{ "id": "uuid", "publisherCode": "INST_MEDICAL", "summary": {...} }],
    "responseTimeMs": 12
  }
}
```

#### POST `/api/v1/exchange/query` — Query data from another institution
```json
// Headers: X-Institution-Code: INST_DMV  X-Institution-Key: sk_bridge_...

// Request
{
  "nationalId": "39003150011",
  "dataType": "VISION_TEST",
  "targetCode": "INST_MEDICAL"
}

// Response 200 (SUCCESS)
{
  "success": true,
  "data": {
    "exchangeId": 2,
    "requestingCode": "INST_DMV",
    "targetCode": "INST_MEDICAL",
    "nationalId": "39003150011",
    "dataType": "VISION_TEST",
    "result": "SUCCESS",
    "records": [{ "id": "uuid", "recordRef": "MED-2024-V-001", "summary": {...} }],
    "responseTimeMs": 18
  }
}

// Response 403 (no permission)
{ "status": 403, "error": "Permission Denied", "message": "INST_DMV does not have permission..." }
```

#### GET `/api/v1/exchange/data/{nationalId}/{dataType}` — Direct data lookup
```
GET /api/v1/exchange/data/39003150011/VISION_TEST
Headers: X-Institution-Code: INST_DMV
         X-Institution-Key: sk_bridge_...
```

---

### Audit Log  *(ROLE_ADMIN — Bearer JWT)*

#### GET `/api/v1/audit/exchanges` — Full exchange log with filters
```
GET /api/v1/audit/exchanges?institutionCode=INST_DMV&dataType=VISION_TEST&result=DENIED&page=0&size=20
GET /api/v1/audit/exchanges?from=2024-01-01T00:00:00&to=2024-01-31T23:59:59
```

#### GET `/api/v1/audit/exchanges/{nationalId}` — All exchanges for a citizen
```
GET /api/v1/audit/exchanges/39003150011
```

---

## Security Model

| Layer | Mechanism |
|-------|-----------|
| Admin auth | HMAC-SHA256 JWT, 24h expiry |
| Institution auth | BCrypt-hashed API key (12 rounds), constant-time comparison |
| Rate limiting | 50 req/min per institution; 100 req/min per IP for admin |
| Permission enforcement | Every exchange checks the `institution_permissions` table |
| Audit trail | Every exchange logged immutably — no updates/deletes on exchange_logs |
| Data minimalism | No raw PII stored — only metadata + external record references |

---

## Project Structure

```
com.sanly.bridge
├── BridgeApplication.java
├── config/
│   ├── SecurityConfig.java           Filter chain configuration
│   ├── JwtTokenProvider.java         Admin JWT generation & validation
│   ├── JwtAuthenticationFilter.java  Extracts JWT from Authorization header
│   ├── InstitutionAuthentication.java Spring Security token for institutions
│   ├── InstitutionAuthFilter.java    Validates X-Institution-Key (exchange paths only)
│   ├── RateLimitingFilter.java       Dual-mode rate limiter (IP / institution)
│   ├── AuthEntryPoint.java           401 JSON response
│   ├── AsyncConfig.java              DelegatingSecurityContextExecutor
│   ├── DataInitializer.java          Seeds admin user at startup
│   └── UserDetailsImpl.java          Spring Security principal wrapper
├── controller/
│   ├── AuthController.java
│   ├── InstitutionController.java
│   ├── PermissionController.java
│   ├── ExchangeController.java
│   └── AuditController.java
├── service/
│   ├── ApiKeyService.java            Secure key generation + BCrypt verify
│   ├── InstitutionService/Impl.java
│   ├── PermissionService/Impl.java
│   ├── ExchangeServiceImpl.java      Core exchange logic + audit persistence
│   └── UserDetailsServiceImpl.java
├── repository/
│   ├── InstitutionRepository.java
│   ├── InstitutionPermissionRepository.java
│   ├── ExchangeLogRepository.java    Custom filter query
│   ├── PublishedDataRepository.java  Active + non-expired lookup
│   └── UserRepository.java
├── entity/
│   ├── Institution.java
│   ├── InstitutionPermission.java    Soft-deletable permission rows
│   ├── ExchangeLog.java              Immutable audit records
│   ├── PublishedData.java            Metadata registry (UUID PK)
│   ├── User.java
│   ├── DataType.java                 (enum) 8 data types
│   ├── InstitutionStatus.java        (enum) ACTIVE | SUSPENDED | PENDING
│   ├── ExchangeResult.java           (enum) SUCCESS | DENIED | NOT_FOUND | ERROR
│   └── OperationType.java            (enum) QUERY | PUBLISH
├── dto/ (request/ + response/)
└── exception/
    ├── GlobalExceptionHandler.java
    └── … domain exceptions
```

---

## Database Schema

| Table | Description |
|-------|-------------|
| `institutions` | Registered agencies with hashed API keys |
| `institution_publishable_types` | Which data types each institution may publish |
| `institution_permissions` | Who may query what from whom (soft-deletable) |
| `exchange_logs` | **Immutable** audit trail of every exchange |
| `published_data` | Metadata registry of published records |
| `users` | Admin accounts for management endpoints |
| `user_roles` | User-role join table |

---

## What's Next (SANLY Step 3+)

- **TM-Login** — Keycloak SSO for all portals
- **Institution Portals** — Medical, DMV, Tax each become standalone services
  that authenticate with SANLY Bridge to share data
