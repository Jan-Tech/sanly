# SANLY DMV Service

The **SANLY DMV Service** is the transport/driving license authority portal of the SANLY e-governance platform. It is the **first service that queries rather than publishes** through SANLY Bridge.

When a DMV officer issues a driving license, the system automatically checks the citizen's vision test result from SANLY Bridge in real time. No direct database link exists between Medical and DMV — all inter-agency communication goes through the bridge.

---

## Platform Position

```
citizen-registry (8080) ─── verifies citizens ───────────────────► DMV (8083)
sanly-bridge (8081)     ─── provides vision test data ───────────► DMV (8083)
sanly-medical (8082)    ─── publishes VISION_TEST to bridge ──────► bridge ──► DMV
```

---

## The Complete End-to-End Demo Flow

```
① Register citizen         → POST /api/v1/citizens              (citizen-registry:8080)
                             ← TM-NIN: 39003150011

② Submit vision test       → POST /api/v1/records               (sanly-medical:8082)
   (doctor at clinic)        → async publish to SANLY Bridge
                             ← recordId: uuid, result: PASS

③ Apply for license        → POST /api/v1/applications           (sanly-dmv:8083)
   (officer on behalf of     { citizenNationalId, requestedCategory: B }
    citizen)                 ← applicationId: uuid

④ Approve application      → POST /api/v1/applications/{id}/process
                             { status: APPROVED }

⑤ Issue license            → POST /api/v1/licenses
                             { applicationId: uuid }
                             Internally:
                               ✓ Application is APPROVED
                               ✓ Citizen is ACTIVE (citizen-registry)
                               ✓ No duplicate ACTIVE license for category B
                               ✓ Bridge: VISION_TEST → result=PASS, not expired
                             ← licenseNumber: TM-DL-2026000001

⑥ Police verify license    → GET /api/v1/licenses/verify/TM-DL-2026000001   [PUBLIC, no auth]
                             ← { valid: true, status: ACTIVE, expiresAt: 2030-... }
```

---

## First-Time Setup

### 1. Register INST_DMV on SANLY Bridge
```bash
curl -X POST http://localhost:8081/api/v1/institutions \
  -H "Authorization: Bearer <BRIDGE_ADMIN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{"institutionCode":"INST_DMV","name":"State Traffic Safety Authority","publishableTypes":[]}'
# → copy returned apiKey into BRIDGE_INSTITUTION_KEY in .env
```

### 2. Grant INST_DMV permission to query VISION_TEST from INST_MEDICAL
```bash
curl -X POST http://localhost:8081/api/v1/permissions \
  -H "Authorization: Bearer <BRIDGE_ADMIN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{"requestingCode":"INST_DMV","targetCode":"INST_MEDICAL","dataType":"VISION_TEST"}'
```

### 3. Get a Citizen Registry institution token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"institution","password":"Inst@Sanly2024!"}'
# → copy token into CITIZEN_REGISTRY_TOKEN in .env
```

### 4. Start
```bash
cp .env.example .env  # fill in all vars
docker-compose up --build
# Swagger: http://localhost:8083/swagger-ui.html
```

---

## API Reference

### POST `/api/v1/auth/login`
```json
{ "username": "admin", "password": "Admin@Dmv2024!" }
```

---

### Applications *(ROLE_ADMIN, ROLE_OFFICER)*

#### POST `/api/v1/applications`
```json
{ "citizenNationalId": "39003150011", "requestedCategory": "B" }
```

#### GET `/api/v1/applications/{applicationId}`
#### GET `/api/v1/applications/citizen/{nationalId}?page=0&size=20`

#### POST `/api/v1/applications/{applicationId}/process`
```json
// Approve
{ "status": "APPROVED" }

// Reject
{ "status": "REJECTED", "rejectionReason": "Incomplete documentation" }
```

---

### Licenses *(ROLE_ADMIN, ROLE_OFFICER — except verify which is PUBLIC)*

#### POST `/api/v1/licenses` — Issue a license
```json
{ "applicationId": "uuid-of-approved-application", "notes": "Optional officer notes" }
```

**Response on success (201):**
```json
{
  "success": true,
  "message": "License issued: TM-DL-2026000001",
  "data": {
    "licenseId": "uuid",
    "citizenNationalId": "39003150011",
    "licenseNumber": "TM-DL-2026000001",
    "category": "B",
    "issuedAt": "2026-05-18T10:30:00",
    "expiresAt": "2030-05-18T10:30:00",
    "issuedByOfficerId": 2,
    "status": "ACTIVE",
    "visionTestRef": "uuid-of-medical-record"
  }
}
```

**Failure responses (422 Unprocessable Entity):**
```json
// No vision test found
{ "error": "Vision Test Required",
  "message": "No valid vision test found in SANLY registry. Citizen must visit a registered clinic first." }

// Expired
{ "error": "Vision Test Expired",
  "message": "Vision test expired on 2025-01-14. Citizen must renew at a registered clinic." }

// Fail result
{ "error": "Vision Test Failed",
  "message": "Vision test result is FAIL. License cannot be issued." }
```

#### GET `/api/v1/licenses/{licenseId}`
#### GET `/api/v1/licenses/citizen/{nationalId}?page=0&size=20`

#### GET `/api/v1/licenses/verify/{licenseNumber}` — **PUBLIC — No Auth**
```json
// Valid license
{
  "success": true,
  "data": {
    "licenseNumber": "TM-DL-2026000001",
    "citizenNationalId": "39003150011",
    "category": "B",
    "issuedAt": "2026-05-18T10:30:00",
    "expiresAt": "2030-05-18T10:30:00",
    "status": "ACTIVE",
    "valid": true
  }
}
```

#### PATCH `/api/v1/licenses/{licenseId}/status`
```json
{ "status": "SUSPENDED", "reason": "Traffic violation pending investigation" }
// Allowed: SUSPENDED, REVOKED
```

---

### Officers *(ROLE_ADMIN)*

#### POST `/api/v1/officers`
```json
{
  "nationalId": "59003150011",
  "firstName": "Kakamyrat",
  "lastName": "Durdyýew",
  "officeRegion": "Ahal",
  "username": "officer.durdyyew",
  "password": "SecurePass@2024"
}
```
#### GET `/api/v1/officers`
#### PATCH `/api/v1/officers/{officerId}/status`

---

## License Number Format

`TM-DL-YYYYNNNNNN`

| Part | Example | Meaning |
|------|---------|---------|
| `TM-DL-` | `TM-DL-` | Turkmenistan Driving License prefix |
| `YYYY` | `2026` | Year of issuance |
| `NNNNNN` | `000001` | 6-digit zero-padded sequence for this year |

Full example: `TM-DL-2026000001`

Generation is pessimistic-locked per year (same pattern as TM-NIN generation in citizen-registry) to guarantee uniqueness under concurrent issuances.

---

## Vision Test Validation Logic

```
GET /api/v1/exchange/data/{nationalId}/VISION_TEST  ← SANLY Bridge query

if no records returned:
  → 422: "No valid vision test found..."

take the most recent record (bridge returns sorted by publishedAt DESC):

if record.expiresAt is in the past:
  → 422: "Vision test expired on {date}..."

if record.summary.result != "PASS":
  → 422: "Vision test result is FAIL..."

if all checks pass:
  → generate TM-DL number, save license, mark audit SUCCESS
```

Every attempt — success or failure — is logged to `issuance_audit_log` via `@Transactional(REQUIRES_NEW)` so failures are persisted even when the outer transaction rolls back.

---

## Integration Failure Handling

| Scenario | HTTP Response |
|----------|---------------|
| Citizen Registry down | 503 Service Unavailable |
| SANLY Bridge down | 503 Service Unavailable |
| INST_DMV not registered on bridge | 403 Insufficient Bridge Permission |
| INST_DMV lacks VISION_TEST permission | 403 Insufficient Bridge Permission |

---

## Project Structure

```
com.sanly.dmv
├── DmvApplication.java
├── client/
│   ├── BridgeRecord.java            DTO for bridge response records
│   ├── BridgeQueryService.java      Queries SANLY Bridge (consumer, not publisher)
│   └── CitizenRegistryClient.java   Verifies citizen identity
├── config/
│   ├── SecurityConfig.java          Public: GET /licenses/verify/**
│   ├── JwtTokenProvider.java        Embeds officerId in token claims
│   ├── JwtAuthenticationFilter.java
│   ├── UserDetailsImpl.java         Carries officerId
│   ├── RestTemplateConfig.java
│   ├── RateLimitingFilter.java
│   ├── AuthEntryPoint.java
│   └── DataInitializer.java
├── controller/
│   ├── AuthController.java
│   ├── CitizenLookupController.java
│   ├── OfficerController.java
│   ├── LicenseApplicationController.java
│   └── DrivingLicenseController.java   (PUBLIC verify endpoint inside)
├── service/
│   ├── OfficerServiceImpl.java
│   ├── LicenseApplicationServiceImpl.java
│   ├── DrivingLicenseServiceImpl.java   Core issuance logic + vision check
│   ├── IssuanceAuditService.java        REQUIRES_NEW tx for audit
│   └── UserDetailsServiceImpl.java
├── repository/
│   ├── OfficerRepository.java
│   ├── LicenseApplicationRepository.java
│   ├── DrivingLicenseRepository.java
│   ├── IssuanceAuditLogRepository.java
│   └── LicenseSequenceRepository.java   Pessimistic-locked per year
├── entity/
│   ├── DmvOfficer.java
│   ├── LicenseApplication.java
│   ├── DrivingLicense.java
│   ├── IssuanceAuditLog.java        Immutable issuance audit
│   ├── LicenseSequence.java         Per-year counter
│   ├── LicenseCategory.java         A, B, C, D, BE, CE
│   ├── LicenseStatus.java           ACTIVE, SUSPENDED, EXPIRED, REVOKED
│   ├── ApplicationStatus.java       PENDING, APPROVED, REJECTED
│   └── OfficerStatus.java           ACTIVE, SUSPENDED
├── dto/ (request/ + response/)
└── exception/
    ├── VisionTestRequiredException.java   → 422
    ├── VisionTestExpiredException.java    → 422
    ├── VisionTestFailedException.java     → 422
    ├── BridgeUnavailableException.java    → 503
    ├── InsufficientPermissionException.java → 403
    └── GlobalExceptionHandler.java
```

---

## What's Next

**Step 5 — SANLY Tax Service** (port 8084): query TAX_STATUS from bridge
**Step 6 — SANLY Police** (port 8085): publish CRIMINAL_RECORD, DMV queries it before license renewal
