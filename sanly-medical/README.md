# SANLY Medical Service

The **SANLY Medical Service** is the first institution portal in the SANLY e-governance platform. It allows licensed clinics and doctors to submit medical test results for citizens. These results are automatically published to SANLY Bridge so that other institutions (e.g. DMV when issuing a driving license) can securely query them.

---

## Platform Position

```
sanly-bridge  (port 8081)        ← Data exchange layer
      ▲  ▲
      │  └─────────── queries (e.g. INST_DMV checking VISION_TEST)
      │
sanly-medical (port 8082)        ← You are here
      │
      └─ publishes on every record create/update
      
sanly citizen-registry (port 8080) ← Verifies citizens exist before a record is saved
```

---

## First-Time Setup

Before the medical service can talk to the platform, you need to:

### 1. Obtain a Citizen Registry token
In citizen-registry, create or use an existing `ROLE_INSTITUTION` user account and get its JWT:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"institution","password":"Inst@Sanly2024!"}'
# → copy the token into CITIZEN_REGISTRY_TOKEN in .env
```

### 2. Register INST_MEDICAL in SANLY Bridge
```bash
curl -X POST http://localhost:8081/api/v1/institutions \
  -H "Authorization: Bearer <BRIDGE_ADMIN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "institutionCode": "INST_MEDICAL",
    "name": "Ministry of Health Medical Records",
    "publishableTypes": ["VISION_TEST", "MEDICAL_CLEARANCE"]
  }'
# → copy the returned apiKey into BRIDGE_INSTITUTION_KEY in .env
```

### 3. Start the medical service
```bash
cp .env.example .env  # fill in all vars
docker-compose up --build
# API:     http://localhost:8082
# Swagger: http://localhost:8082/swagger-ui.html
```

---

## How the Data Flow Works

```
Doctor submits POST /api/v1/records
  ├── 1. CitizenRegistryClient.verify(nationalId)
  │         → GET /api/v1/citizens/{nationalId}/verify on citizen-registry
  │         → 404 if citizen not found or not ACTIVE
  ├── 2. Save MedicalRecord to local PostgreSQL
  │         → notes column AES-256-GCM encrypted
  │         → UUID primary key (serves as bridge recordRef)
  └── 3. BridgePublisherService.publish(record)  ← @Async, non-blocking
            → POST /api/v1/exchange/publish on sanly-bridge
            → summary: {testType, result, testedAt, clinicId}
            → bridge.published = true on success
            → on failure: logged only, local record unaffected
```

---

## Running Locally (without Docker)

```bash
# PostgreSQL must be running on localhost:5434 with database sanly_medical
export DB_URL=jdbc:postgresql://localhost:5434/sanly_medical
export DB_USERNAME=medical_user
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-at-least-32-char-jwt-secret!!
export ENCRYPTION_KEY=your-exactly-32-char-enc-key!!!!
export ADMIN_PASSWORD=Admin@Medical2024!
export CITIZEN_REGISTRY_TOKEN=<jwt-from-citizen-registry>
export BRIDGE_INSTITUTION_KEY=sk_bridge_<64-hex>

mvn spring-boot:run
```

---

## API Reference

### Authentication
#### POST `/api/v1/auth/login`
```json
// Request
{ "username": "admin", "password": "Admin@Medical2024!" }

// Response 200
{
  "success": true,
  "data": {
    "token": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "username": "dr.ahmedov",
    "roles": ["ROLE_DOCTOR"],
    "doctorId": 5,
    "clinicId": 2
  }
}
```

---

### Citizen Lookup *(ROLE_ADMIN, ROLE_DOCTOR)*
#### GET `/api/v1/citizens/{nationalId}`
Proxies to SANLY Citizen Registry verify endpoint.
```json
// Response 200
{
  "success": true,
  "data": { "nationalId": "39003150011", "exists": true, "active": true, "fullName": "Ataýew Merdan Gurbanowiç" }
}
// Response 404 — citizen not in registry
// Response 503 — citizen-registry unreachable
```

---

### Medical Records *(ROLE_ADMIN, ROLE_DOCTOR)*

#### POST `/api/v1/records` — Submit a test result
```json
// Request (as ROLE_DOCTOR — doctor's clinicId used automatically)
{
  "citizenNationalId": "39003150011",
  "testType": "VISION_TEST",
  "notes": "Left eye 20/30, right eye 20/20. Mild astigmatism noted.",
  "testedAt": "2024-01-15T09:30:00",
  "expiresAt": "2025-01-15T00:00:00"
}

// Response 201
{
  "success": true,
  "message": "Medical record created and submitted to SANLY Bridge",
  "data": {
    "recordId": "550e8400-e29b-41d4-a716-446655440000",
    "citizenNationalId": "39003150011",
    "doctorId": 5,
    "clinicId": 2,
    "testType": "VISION_TEST",
    "result": "PENDING",
    "notes": "Left eye 20/30...",
    "testedAt": "2024-01-15T09:30:00",
    "expiresAt": "2025-01-15T00:00:00",
    "bridgePublished": false,
    "createdAt": "2024-01-15T09:31:00"
  }
}
```
Note: `bridgePublished` is false immediately — the async publish happens in the background. Refresh the record a moment later to see `bridgePublished: true`.

Test types: `VISION_TEST | HEARING_TEST | BLOOD_TEST | PHYSICAL_EXAM | MENTAL_HEALTH_EVAL | DRUG_SCREENING | GENERAL_CHECKUP`

#### GET `/api/v1/records/{recordId}` — Get a specific record
Doctors: only see records from their own clinic. Admins: see all.

#### GET `/api/v1/records/citizen/{nationalId}` — All records for a citizen
```
GET /api/v1/records/citizen/39003150011?page=0&size=20
```
Doctors: clinic-scoped. Admins: all clinics.

#### PATCH `/api/v1/records/{recordId}/result` — Update result
```json
// Request
{ "result": "PASS", "notes": "Follow-up exam confirmed normal vision." }

// Response 200
{ "success": true, "message": "Result updated and republished to SANLY Bridge", "data": {...} }
```
Result values: `PASS | FAIL | PENDING`

---

### Clinic Management *(ROLE_ADMIN)*

#### POST `/api/v1/clinics`
```json
{
  "name": "National Eye Center",
  "licenseNumber": "MH-CL-2024-001",
  "region": "Ahal",
  "address": "Garaşsyzlyk şaýoly 15, Ashgabat",
  "phone": "+993 12 123456"
}
```
#### GET `/api/v1/clinics` — List all clinics
#### PATCH `/api/v1/clinics/{clinicId}/status`
```json
{ "status": "SUSPENDED", "reason": "License renewal pending" }
```

---

### Doctor Management *(ROLE_ADMIN)*

#### POST `/api/v1/doctors`
```json
{
  "nationalId": "39003150011",
  "firstName": "Ýunus",
  "lastName": "Ahmedow",
  "specialization": "Ophthalmology",
  "licenseNumber": "DR-2024-001",
  "clinicId": 1,
  "username": "dr.ahmedow",
  "password": "SecurePass@123"
}
```
#### GET `/api/v1/doctors` — List all doctors
#### PATCH `/api/v1/doctors/{doctorId}/status`
```json
{ "status": "ACTIVE" }
```

---

## Security

| Mechanism | Detail |
|-----------|--------|
| Authentication | HMAC-SHA256 JWT, 24h expiry |
| Password hashing | BCrypt cost factor 12 |
| Notes encryption | AES-256-GCM per-value (random IV) |
| Clinic scoping | Doctors can only read/write their own clinic's records |
| Rate limiting | 100 req/min per IP (Bucket4j in-memory) |
| External auth | Bearer JWT for citizen-registry; API key for SANLY Bridge |

---

## Project Structure

```
com.sanly.medical
├── MedicalApplication.java
├── client/
│   ├── CitizenRegistryClient.java   HTTP → citizen-registry /verify
│   └── BridgePublisherService.java  @Async HTTP → sanly-bridge /publish
├── config/
│   ├── SecurityConfig.java
│   ├── JwtTokenProvider.java        Embeds doctorId + clinicId in JWT claims
│   ├── JwtAuthenticationFilter.java
│   ├── UserDetailsImpl.java         Carries doctorId + clinicId for scoping
│   ├── EncryptionService.java       AES-256-GCM for notes
│   ├── EncryptedStringConverter.java JPA AttributeConverter
│   ├── AsyncConfig.java             DelegatingSecurityContextExecutor
│   ├── RestTemplateConfig.java      Timeouts for external calls
│   ├── RateLimitingFilter.java
│   ├── AuthEntryPoint.java
│   └── DataInitializer.java         Seeds admin account
├── controller/
│   ├── AuthController.java
│   ├── CitizenLookupController.java  Proxy to citizen-registry
│   ├── ClinicController.java
│   ├── DoctorController.java
│   └── MedicalRecordController.java
├── service/
│   ├── ClinicService/Impl.java
│   ├── DoctorService/Impl.java
│   ├── MedicalRecordService/Impl.java  Core logic + integration orchestration
│   └── UserDetailsServiceImpl.java
├── repository/
│   ├── ClinicRepository.java
│   ├── DoctorRepository.java
│   └── MedicalRecordRepository.java   findUnpublished() for retry jobs
├── entity/
│   ├── Clinic.java
│   ├── Doctor.java         Auth credentials + clinicId
│   ├── MedicalRecord.java  UUID PK, encrypted notes, bridgePublished flag
│   ├── TestType.java       (enum) 7 test types
│   ├── RecordResult.java   (enum) PASS | FAIL | PENDING
│   ├── ClinicStatus.java   (enum) ACTIVE | SUSPENDED
│   └── DoctorStatus.java   (enum) ACTIVE | SUSPENDED
├── dto/ (request/ + response/)
└── exception/
    ├── GlobalExceptionHandler.java
    ├── CitizenNotFoundException.java
    ├── CitizenRegistryUnavailableException.java  → 503
    ├── ClinicAccessDeniedException.java          → 403
    └── … other domain exceptions
```

---

## Database Schema

| Table | Purpose |
|-------|---------|
| `clinics` | Registered medical facilities |
| `doctors` | Doctor accounts (also admin accounts with null clinicId) |
| `doctor_roles` | Role assignments |
| `medical_records` | Test results (UUID PK, encrypted notes, bridge publish tracking) |

---

## TestType → Bridge DataType Mapping

| Medical TestType | SANLY Bridge DataType |
|------------------|-----------------------|
| `VISION_TEST`    | `VISION_TEST`         |
| All others       | `MEDICAL_CLEARANCE`   |

---

## What's Next

**Step 4 — SANLY DMV Service** (port 8083):
- Doctor submits VISION_TEST → published to Bridge
- DMV queries `GET /api/v1/exchange/data/{nationalId}/VISION_TEST`
- If PASS and valid → issue driving license
