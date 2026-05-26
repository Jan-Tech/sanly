# sanly-education

Education authority portal for the SANLY e-governance platform. Port **8090**.

## Purpose

Education officers register schools and universities, enroll students, record grades, and issue diplomas.
Employers and institutions verify diplomas through SANLY Bridge — fake diplomas are impossible.
The civil registry's school enrollment queue (children turning 6) feeds directly into this service.

## Entities

| Entity | Description |
|---|---|
| `EducationOfficer` | Officers and admins who manage institutions/records |
| `EducationInstitution` | Schools and universities (`TM-EDU-NNNN` codes) |
| `Enrollment` | Student enrollment in an institution, including PENDING intake from civil registry |
| `AcademicRecord` | Per-year grade/GPA records (grade, gpa, notes AES-256-GCM encrypted) |
| `Diploma` | Issued certificates (`TM-DIP-YYYYNNNNNN`), published to SANLY Bridge |

## Endpoints

### Auth
| Method | Path | Auth |
|---|---|---|
| `POST` | `/api/v1/education/auth/login` | Public |

### Institutions (ADMIN only)
| Method | Path |
|---|---|
| `POST` | `/api/v1/education/institutions` |
| `GET` | `/api/v1/education/institutions` |
| `GET` | `/api/v1/education/institutions/{code}` |
| `PATCH` | `/api/v1/education/institutions/{code}/status` |

### Enrollments (OFFICER+)
| Method | Path | Notes |
|---|---|---|
| `POST` | `/api/v1/education/enrollments` | Verifies citizen in registry |
| `GET` | `/api/v1/education/enrollments/{id}` | |
| `GET` | `/api/v1/education/enrollments/citizen/{nin}` | Full history |
| `PATCH` | `/api/v1/education/enrollments/{id}/status` | GRADUATED / DROPPED / TRANSFERRED |
| `POST` | `/api/v1/education/enrollments/pending-intake` | Internal — X-Life-Event-Key |
| `GET` | `/api/v1/education/enrollments/pending` | Work queue for officers |

### Academic Records (OFFICER+)
| Method | Path |
|---|---|
| `POST` | `/api/v1/education/records` |
| `GET` | `/api/v1/education/records/citizen/{nin}` |

### Diplomas (OFFICER+ / ADMIN)
| Method | Path | Auth |
|---|---|---|
| `POST` | `/api/v1/education/diplomas` | OFFICER+ (requires GRADUATED enrollment) |
| `GET` | `/api/v1/education/diplomas/{code}` | Authenticated |
| `GET` | `/api/v1/education/diplomas/citizen/{nin}` | Authenticated |
| `GET` | `/api/v1/education/diplomas/verify/{code}` | **Public** — for employers |
| `PATCH` | `/api/v1/education/diplomas/{code}/revoke` | ADMIN only |
| `GET` | `/api/v1/education/diplomas` | ADMIN only |

### Officers (ADMIN only)
| Method | Path |
|---|---|
| `POST` | `/api/v1/education/officers` |
| `GET` | `/api/v1/education/officers` |
| `PATCH` | `/api/v1/education/officers/{id}/status` |

## Diploma verification flow

```
Employer / University
        │
        │  GET /education/api/v1/education/diplomas/verify/TM-DIP-2024000001
        │  (via sanly-gateway — no auth required)
        │
        ▼
sanly-education returns: { diplomaCode, citizenNationalId, programName,
                           programLevel, graduationDate, honors, status }

status = VALID  → diploma is genuine ✓
status = REVOKED → diploma has been invalidated ✗
```

The same diploma data is also queryable via SANLY Bridge (dataType=`EDUCATION_DIPLOMA`)
by any institution with the appropriate permission.

## Life-event integration

When civil registry registers a birth, it queues the child for school enrollment 6 years later.
On September 1st each year, `EnrollmentReminderService` (in sanly-civil) notifies parents
**and** calls `POST /api/v1/education/enrollments/pending-intake` (authenticated via `X-Life-Event-Key`)
to create a PENDING enrollment record for education officers to process.

## Security

- JWT auth (HMAC-SHA256, separate signing key per deployment)
- `BCryptPasswordEncoder(12)` for passwords
- AES-256-GCM for `grade`, `gpa`, `notes` fields
- Rate limiting: 100 req/min per IP (Bucket4j)
- `LifeEventKeyFilter` guards the pending-intake endpoint
- Officers can only manage their assigned institution (enforced in service layer)
- Public diploma verify endpoint requires no auth

## Environment variables

| Variable | Description |
|---|---|
| `JWT_SECRET` | Min 32 chars (HMAC-SHA256) |
| `ENCRYPTION_KEY` | Exactly 32 chars (AES-256) |
| `ADMIN_PASSWORD` | Default admin account password |
| `CITIZEN_REGISTRY_URL` | Base URL for citizen verification |
| `CITIZEN_REGISTRY_TOKEN` | ROLE_INSTITUTION JWT from citizen-registry |
| `BRIDGE_URL` | SANLY Bridge base URL |
| `BRIDGE_INSTITUTION_KEY` | API key for INST_EDUCATION |
| `NOTIFICATION_URL` | sanly-notifications base URL |
| `NOTIFICATION_SERVICE_KEY` | Service auth key for notifications |
| `LIFE_EVENT_SERVICE_KEY` | Shared key for civil registry automation |

## Setup (full platform)

```bash
# 1. Start the platform
make up  # or docker-compose up --build

# 2. Log in as admin (via gateway)
POST http://localhost:8080/education/api/v1/education/auth/login
{"username":"admin","password":"<EDUCATION_ADMIN_PASSWORD>"}

# 3. Register INST_EDUCATION on sanly-bridge
POST http://localhost:8080/bridge/api/v1/institutions
{"name":"SANLY Education","code":"INST_EDUCATION"}
# Copy the one-time API key → BRIDGE_INSTITUTION_KEY_EDUCATION in .env

# 4. Register a school
POST http://localhost:8080/education/api/v1/education/institutions
{"name":"School No. 1","type":"PRIMARY_SCHOOL","region":"Aşgabat","address":"..."}

# 5. Create an officer for that school
POST http://localhost:8080/education/api/v1/education/officers
{"nationalId":"...","firstName":"Aýna","lastName":"Amanowa",
 "username":"officer1","password":"...","institutionCode":"TM-EDU-0001","role":"OFFICER"}
```

## Swagger UI

Available at `http://localhost:8080/education/swagger-ui.html` (via gateway)
or `http://localhost:8090/swagger-ui.html` (direct, local dev only).
