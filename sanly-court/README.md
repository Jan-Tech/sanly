# sanly-court — Judicial Authority Service

Port **8094** · PostgreSQL DB `sanly_court` (port 5446)

## Purpose

Manages the full judicial lifecycle for the SANLY e-governance platform:
- Court case filing and progress tracking
- Digital document submission with SHA-256 signature verification
- Judge verdict issuance with automatic criminal record registration
- Court fine issuance, payment collection, and overdue enforcement

---

## Seeded Courts

| Code       | Name                                     | Type          | Region    |
|------------|------------------------------------------|---------------|-----------|
| TM-CRT-001 | Ashgabat District Court No.1             | DISTRICT      | Ahal      |
| TM-CRT-002 | Ashgabat City Court                      | REGIONAL      | Ahal      |
| TM-CRT-003 | Supreme Court of Turkmenistan            | SUPREME       | Ahal      |
| TM-CRT-004 | Balkan Regional Court                    | REGIONAL      | Balkan    |
| TM-CRT-005 | Mary Regional Court                      | REGIONAL      | Mary      |
| TM-CRT-006 | Lebap Regional Court                     | REGIONAL      | Lebap     |
| TM-CRT-007 | Dashoguz Regional Court                  | REGIONAL      | Dashoguz  |
| TM-CRT-008 | Ahal Regional Court                      | REGIONAL      | Ahal      |
| TM-CRT-009 | Turkmenistan Arbitration Court           | ARBITRATION   | Ahal      |
| TM-CRT-010 | Ashgabat Administrative Court            | ADMINISTRATIVE| Ahal      |

---

## Key Flows

### Traffic Fine Flow

```
Officer issues fine (POST /fines)
  → Status: OUTSTANDING
  → Notification: COURT_FINE_ISSUED to citizen

Citizen submits payment proof (PATCH /fines/{code}/citizen-pay)
  → Status: PENDING_VERIFICATION
  → Notification: FINE_PAYMENT_PENDING_VERIFICATION to clerk

Clerk verifies (PATCH /fines/{code}/verify-payment)
  → Status: PAID
  → Notification: FINE_PAYMENT_CONFIRMED to citizen

[If unpaid past dueDate]
  FineOverdueScheduler (daily 08:00)
  → Status: OVERDUE
  → Notification: FINE_OVERDUE to citizen
```

### Digital Document Signing

Every submitted document receives a SHA-256 digital signature:

```
signature = SHA-256(content + submitterNationalId + submittedAt.toString())
```

The `/documents/{docCode}/verify` endpoint recomputes the signature and returns:
```json
{ "valid": true, "documentCode": "TM-DOC-2025000001", "submittedAt": "..." }
```

This allows any party (citizens, courts, other agencies) to verify a document's
authenticity without storing the plaintext content outside the encrypted database.

### Verdict → Criminal Record Flow

```
Judge issues verdict (POST /cases/{caseNumber}/verdict)
  → CourtCase status: DECIDED (or CLOSED)
  → COURT_ORDER published to Bridge (async, @Async)
  → If verdictType == GUILTY:
      PoliceServiceClient.addCriminalRecord() — fire-and-forget
      POST /api/v1/criminal-records/court-conviction
      Header: X-Court-Service-Key
      [If police rejects → warning logged, Bridge COURT_ORDER is authoritative]
  → Notification: COURT_VERDICT_ISSUED to both plaintiff and defendant
```

---

## Case Status Progression

```
FILED → UNDER_REVIEW → HEARING_SCHEDULED → IN_PROGRESS → DECIDED
                                                        ↘ APPEALED → (re-enters)
                                                        ↘ DISMISSED
                                                        ↘ CLOSED
```

---

## Scheduled Jobs

| Job                      | Schedule        | Action                                              |
|--------------------------|-----------------|-----------------------------------------------------|
| `FineOverdueScheduler`   | Daily 08:00     | Marks OUTSTANDING fines past dueDate as OVERDUE     |
| `HearingReminderScheduler` | Daily 07:00   | Sends HEARING_REMINDER for hearings within 3 days   |

---

## Security Model

| Endpoint pattern             | Required role           |
|------------------------------|-------------------------|
| `POST /courts`               | ADMIN                   |
| `POST /cases/*/verdict`      | JUDGE (assigned judge)  |
| `PATCH /fines/*/waive`       | JUDGE                   |
| `POST /officers/**`          | ADMIN                   |
| `PATCH /fines/*/citizen-pay` | Any authenticated JWT   |
| `GET /cases/verify/**`       | Public (no auth)        |
| `GET /documents/verify/**`   | Public (no auth)        |
| `GET /fines/verify/**`       | Public (no auth)        |

---

## Environment Variables

| Variable                  | Description                                      |
|---------------------------|--------------------------------------------------|
| `DB_URL`                  | JDBC URL for PostgreSQL                          |
| `JWT_SECRET`              | ≥32 chars, HMAC-SHA256 signing key               |
| `ENCRYPTION_KEY`          | Exactly 32 chars, AES-256-GCM field encryption   |
| `ADMIN_PASSWORD`          | Initial admin account password                   |
| `CITIZEN_REGISTRY_URL`    | Base URL of sanly-registry                       |
| `CITIZEN_REGISTRY_TOKEN`  | Institution JWT for citizen verification         |
| `BRIDGE_URL`              | Base URL of sanly-bridge                         |
| `BRIDGE_INSTITUTION_KEY`  | API key for INST_COURT in bridge                 |
| `NOTIFICATION_URL`        | Base URL of sanly-notifications                  |
| `NOTIFICATION_SERVICE_KEY`| Service key for SANLY_COURT notifications        |
| `POLICE_URL`              | Base URL of sanly-police (for criminal records)  |
| `COURT_POLICE_SERVICE_KEY`| Shared key accepted by police X-Court-Service-Key|

---

## Encrypted Fields

AES-256-GCM encryption (via `EncryptedStringConverter`) is applied to:
- `CourtCase.summary`, `CourtCase.notes`
- `CourtDocument.content`
- `Verdict.summary`, `Verdict.notes`
- `CourtFine.amount`, `CourtFine.paymentProofNote`

---

## Quick Start (standalone)

```bash
# From sanly-court directory
docker-compose up -d

# Create admin JWT
curl -X POST http://localhost:8094/api/v1/court/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
