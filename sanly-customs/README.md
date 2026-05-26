# SANLY Customs — Import/Export & Customs Declarations Authority

Port: **8093** | Database: `sanly_customs` (port 5445) | Institution code: `INST_CUSTOMS`

## Purpose

Allows businesses and citizens to file customs declarations digitally, have duties calculated automatically, pay online, and receive clearance without visiting a customs office. Officers process declarations, inspectors verify cargo, and clearance events are published to SANLY Bridge.

---

## Duty Rate Schedule (HS Code-based)

| HS Chapter | Category | Duty Rate | VAT |
|-----------|----------|-----------|-----|
| 22, 24 | Beverages, alcohol, tobacco | **15%** | 15% of (value + duty) |
| 84, 85 | Machinery, electronics | **2%** | 15% of (value + duty) |
| 30 | Pharmaceuticals | **0%** | 15% of (value + duty) |
| 10 | Cereals, grain | **0%** | 15% of (value + duty) |
| All others | Default | **5%** | 15% of (value + duty) |

**EXPORT / TRANSIT**: Duty 0%, no VAT.

Officers can override the calculated rate by providing `overrideRatePercent` + `dutyRateOverride` justification in the calculate-duties request. The override is logged in the `DutyCalculation` record.

---

## Clearance Flow

```
1. Officer creates declaration (DRAFT)
2. Declarant submits → SUBMITTED + notification sent
3. Officer calculates duties (POST /calculate-duties)
4. Declarant pays duties (PATCH /record-payment)
   → When paid in full: auto-advances to UNDER_REVIEW
5. Inspector may place on HOLD (POST /hold) → inspection recorded
6. Officer clears declaration (PATCH /clear):
   - Checks tax compliance via SANLY Bridge (TAX_STATUS)
   - Verifies dutiesPaid >= dutiesOwed
   - If BLOCKED: returns 409 with reason
   - If OK: status → CLEARED, notification sent, CUSTOMS_CLEARANCE published to Bridge
```

---

## Tax Compliance Gate

Before clearing any declaration, the service queries `SANLY Bridge` for the declarant's `TAX_STATUS`. If the declarant is `NON_COMPLIANT`, clearance is blocked with:

> **409 Conflict**: "Clearance blocked: declarant has outstanding tax obligations"

The declarant must resolve their tax status before the declaration can be cleared.

---

## Declarant Types

- **CITIZEN**: NIN verified against citizen-registry before creating declaration
- **BUSINESS**: Business number queried against Bridge `BUSINESS_REGISTRATION` from `INST_BUSINESS`. Business must be `ACTIVE`.

---

## Scheduled Job

**DutiesReminderScheduler** — runs daily at 09:00:
- Finds all `SUBMITTED` declarations older than 3 days where `dutiesPaid` is null or `"0"`
- Sends `CUSTOMS_DUTIES_REMINDER` notification to declarant

---

## Customs Ports (seeded)

| Code | Name | Type |
|------|------|------|
| TM-PORT-001 | Turkmenbashi International Seaport | SEAPORT |
| TM-PORT-002 | Ashgabat International Airport | AIRPORT |
| TM-PORT-003 | Farap Land Border Crossing | LAND_BORDER |
| TM-PORT-004 | Sarahs Land Border Crossing | LAND_BORDER |
| TM-PORT-005 | Imamnazar Land Border Crossing | LAND_BORDER |
| TM-PORT-006 | Turkmenabad Railway Terminal | RAILWAY |
| TM-PORT-007 | Ashgabat Inland Customs Depot | INLAND_DEPOT |

---

## Encrypted Fields

AES-256-GCM at rest (requires `ENCRYPTION_KEY` = exactly 32 chars):

- `customs_declarations`: `cargo_description`, `declared_value`, `duties_owed`, `duties_paid`, `notes`
- `cargo_items`: `item_description`, `unit_value`, `total_value`
- `duty_calculations`: `calculated_duty_amount`, `vat_amount`, `total_owed`
- `inspection_records`: `findings`, `notes`

---

## API Overview

| Resource | Endpoint |
|----------|----------|
| Auth | `POST /api/v1/customs/auth/login` |
| Ports | `GET/POST /api/v1/customs/ports`, `PATCH /{portCode}/status` (ADMIN) |
| Declarations | `POST /api/v1/customs/declarations` |
| | `GET /{code}`, `/verify/{code}` (public), `/declarant/{id}`, `/port/{portCode}` |
| | `PATCH /{code}/submit`, `/clear`, `/reject`, `/hold`, `/record-payment` |
| Cargo Items | `POST/GET /api/v1/customs/declarations/{code}/items` |
| Duties | `POST /…/{code}/calculate-duties`, `GET /…/{code}/duties` |
| Inspections | `POST/GET /api/v1/customs/declarations/{code}/inspections` |
| Officers | `POST/GET /api/v1/customs/officers`, `PATCH /{id}/status` (ADMIN) |

---

## Local Development

```bash
cp .env.example .env
# Fill in secrets, then:
docker-compose up --build

# Swagger UI
open http://localhost:8093/swagger-ui.html

# Public declaration verify (no auth):
curl http://localhost:8093/api/v1/customs/declarations/verify/TM-CUS-2026000001
```

---

## Notification Events

| Event | Trigger |
|-------|---------|
| `CUSTOMS_DECLARATION_SUBMITTED` | Declaration moves to SUBMITTED |
| `CUSTOMS_DECLARATION_CLEARED` | Declaration cleared |
| `CUSTOMS_DECLARATION_REJECTED` | Declaration rejected |
| `CUSTOMS_DECLARATION_HELD` | Declaration placed on hold |
| `CUSTOMS_DUTIES_REMINDER` | Daily job — unpaid duties > 3 days old |
