# sanly-vehicle — Vehicle Registry Service

Port **8096** · PostgreSQL DB `sanly_vehicle` (port 5447)

## Purpose

Manages vehicle registration, ownership transfers, insurance, and technical inspections for the SANLY e-governance platform. Integrates with police (via Bridge VEHICLE_RECORD) for traffic stop lookups, with civil registry for death-event inheritance automation, and with the Bridge for tax-compliance checks on ownership transfers.

---

## Plate Number Format

**TM-AA-0000** — auto-assigned sequential numbering:

```
TM-AA-0001  TM-AA-0002  ...  TM-AA-9999
TM-AB-0001  TM-AB-0002  ...  TM-AB-9999
...
TM-ZZ-0001  ...  TM-ZZ-9999
```

Total capacity: 26 × 26 × 9,999 = **6,759,324** unique plates.

Uses a `PlateSequence` table with a `PESSIMISTIC_WRITE` lock to prevent duplicates under concurrent registrations.

---

## Ownership Transfer Flow

```
Officer submits transfer (POST /transfers)
  → Vehicle status: UNDER_TRANSFER
  → Transfer status: PENDING
  → Both parties verified in citizen-registry

Officer approves (POST /transfers/{id}/approve)
  → Checks seller's TAX_STATUS via Bridge (blocks if NON_COMPLIANT)
  → Current ownership: TRANSFERRED, ownershipEndDate = today
  → New ownership created: ACTIVE, acquiredVia = PURCHASE|GIFT|INHERITANCE|COURT_ORDER
  → Vehicle status: REGISTERED
  → VEHICLE_RECORD published to Bridge (async)
  → Notifications: VEHICLE_TRANSFER_APPROVED to both parties

Officer rejects (POST /transfers/{id}/reject)
  → Vehicle status: REGISTERED (restored)
  → Transfer status: REJECTED
  → Notification: VEHICLE_TRANSFER_REJECTED to seller
```

---

## Death Registration Integration (Life Events)

When `sanly-civil` registers a death, it calls the vehicle service:

```
POST /api/v1/vehicle/ownership/deceased
Header: X-Life-Event-Key: {LIFE_EVENT_SERVICE_KEY}
Body: { "deceasedNationalId": "..." }
```

The service:
1. Finds all ACTIVE ownerships for the deceased NIN
2. Sets ownership status → INHERITED
3. Sets vehicle status → UNDER_TRANSFER
4. Fires VEHICLE_INHERITANCE_PENDING notification

---

## Police Integration

When a police officer runs a `FULL_CHECK` on a citizen, `sanly-police` queries:
- `VEHICLE_RECORD` from `INST_VEHICLE` via the Bridge

This shows all vehicles registered to the citizen during a traffic stop.

---

## Scheduled Jobs

| Job                       | Schedule       | Action                                             |
|---------------------------|----------------|----------------------------------------------------|
| `InsuranceExpiryScheduler`| Daily 08:00    | Notifies owners of insurance expiring within 14 days |
| `InspectionDueScheduler`  | Daily 08:30    | Notifies owners of inspections due within 30 days   |

---

## Stolen Vehicle Flow

```
Admin marks stolen (PATCH /vehicles/{plate}/status)
  → Vehicle status: STOLEN
  → VEHICLE_RECORD with status=STOLEN published to Bridge (async)
  → Notification: VEHICLE_REPORTED_STOLEN to owner
```

---

## Encrypted Fields

AES-256-GCM encryption (via `EncryptedStringConverter`) applies to:
- `TransferApplication.agreedPrice`
- `InsuranceRecord.policyNumber`
- `TechnicalInspection.findings`

---

## Security Model

| Endpoint                            | Access                |
|-------------------------------------|-----------------------|
| `POST /auth/login`                  | Public                |
| `GET /vehicles/verify/{plate}`      | Public                |
| `GET /insurance/verify/{plate}`     | Public                |
| `POST /ownership/deceased`          | Internal (X-Life-Event-Key) |
| `PATCH /vehicles/{plate}/status`    | ROLE_ADMIN            |
| `POST /officers/**`                 | ROLE_ADMIN            |
| Everything else                     | ROLE_OFFICER or ADMIN |

---

## Environment Variables

| Variable                  | Description                                       |
|---------------------------|---------------------------------------------------|
| `DB_URL`                  | JDBC URL for PostgreSQL                           |
| `JWT_SECRET`              | ≥32 chars, HMAC-SHA256 signing key               |
| `ENCRYPTION_KEY`          | Exactly 32 chars, AES-256-GCM field encryption   |
| `ADMIN_PASSWORD`          | Initial admin account password                   |
| `CITIZEN_REGISTRY_URL`    | Base URL of sanly-registry                       |
| `CITIZEN_REGISTRY_TOKEN`  | Institution JWT for citizen verification         |
| `BRIDGE_URL`              | Base URL of sanly-bridge                         |
| `BRIDGE_INSTITUTION_KEY`  | API key for INST_VEHICLE in bridge               |
| `NOTIFICATION_URL`        | Base URL of sanly-notifications                  |
| `NOTIFICATION_SERVICE_KEY`| Service key for SANLY_VEHICLE notifications      |
| `LIFE_EVENT_SERVICE_KEY`  | Shared key for civil→vehicle death events        |

---

## Quick Start (standalone)

```bash
# From sanly-vehicle directory
docker-compose up -d

# Get admin JWT
curl -X POST http://localhost:8096/api/v1/vehicle/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Register first vehicle
curl -X POST http://localhost:8096/api/v1/vehicle/vehicles \
  -H "Authorization: Bearer {JWT}" \
  -H "Content-Type: application/json" \
  -d '{
    "vin": "WBAWX31060PY46765",
    "make": "Toyota",
    "model": "Camry",
    "year": 2022,
    "color": "White",
    "fuelType": "PETROL",
    "vehicleType": "PASSENGER_CAR",
    "ownerNationalId": "12345678901"
  }'
# Response includes auto-assigned plate: TM-AA-0001
```
