# sanly-pension — Pension Contributions and Retirement Authority

Port **8099** · PostgreSQL DB `sanly_pension` (port 5448)

## Purpose

The authoritative pension service for the SANLY e-governance platform. Tracks employer contributions, manages retirement eligibility calculations, and processes pension payments for citizens of Turkmenistan.

> **Note on sanly-social pension endpoints:** `sanly-social` contains basic `PensionAccount` and pension endpoints that were built as part of an earlier iteration. Those endpoints are now **deprecated** and exist only for backward compatibility. `sanly-pension` is the authoritative service. The migration path:
> 1. Use `GET /pension/api/v1/pension/accounts/citizen/{nationalId}` instead of the social pension endpoint
> 2. `sanly-social`'s `PensionServiceClient` now delegates to `sanly-pension` for eligibility checks
> 3. The social `PensionEligibilityChecker` scheduler should be updated to use `PensionServiceClient`

---

## Employer Registration Flow

```
ADMIN registers employer (POST /employers)
  → Verify businessRegistrationNumber via Bridge BUSINESS_REGISTRATION from INST_BUSINESS
  → Generate TM-EMP-NNNN employer code
  → Create employer officer account (POST /officers with role=EMPLOYER, employerCode=TM-EMP-NNNN)
  → Employer logs in and submits contributions for their employees
```

---

## Retirement Eligibility Formula

**Retirement age:**
- Male (TM-NIN digit 1 = 1, 3, or 5): **60 years**
- Female (TM-NIN digit 1 = 2, 4, or 6): **55 years**

**Minimum contributions:**
- Full pension: 300 months (25 years)
- Partial pension: 180 months (15 years)

**Eligible date:**
```
eligibleAt = max(birthDate + retirementAge, employmentStartDate + 15 years)
```

**Monthly pension calculation:**
```
multiplier = 1.2 if monthsContributed >= 300 (full pension)
           = 0.8 if monthsContributed >= 180 (partial pension)

monthlyPension = (totalContributions / monthsContributed) × multiplier
```

---

## Contribution Rules

- ROLE_EMPLOYER: can only submit contributions for their own employees (enforced by employerCode match)
- ROLE_OFFICER: can submit for any account
- Contributions must be VERIFIED by an officer before they count toward the total
- On VERIFIED: account's `totalContributions`, `totalEmployerContributions`, `totalCitizenContributions` are updated atomically

---

## Death Registration Integration

When `sanly-civil` registers a death:

```
POST /api/v1/pension/accounts/deceased
Header: X-Life-Event-Key: {LIFE_EVENT_SERVICE_KEY}
Body: { "deceasedNationalId": "..." }
```

The service:
1. Finds the pension account for the deceased
2. Sets status → CLOSED
3. Cancels any SCHEDULED payments for this month
4. Fires PENSION_ACCOUNT_CLOSED notification

---

## Scheduled Jobs

| Job                            | Schedule         | Action                                              |
|--------------------------------|------------------|-----------------------------------------------------|
| `MonthlyPensionPaymentScheduler` | 1st of month 07:00 | Creates SCHEDULED payments for all PAYING accounts |
| `ContributionReminderScheduler`  | 25th of month 09:00| Reminds ACTIVE employers who haven't submitted yet  |
| `EligibilityCheckScheduler`      | Daily 06:00        | Marks ACCUMULATING accounts ELIGIBLE when date arrives |

---

## Code Formats

| Entity   | Format               | Example              |
|----------|----------------------|----------------------|
| Employer | TM-EMP-NNNN          | TM-EMP-0001          |
| Account  | TM-PEN-YYYYNNNNNN    | TM-PEN-2024000001    |

---

## Encrypted Fields

AES-256-GCM encryption applies to:
- `PensionAccount.totalContributions`, `.totalEmployerContributions`, `.totalCitizenContributions`
- `ContributionRecord.employerAmount`, `.citizenAmount`, `.totalAmount`
- `PensionPayment.amount`
- `RetirementApplication.monthlyPensionAmount`

---

## Security Model

| Endpoint pattern                   | Required role              |
|------------------------------------|----------------------------|
| `POST /auth/login`                 | Public                     |
| `POST /employers/**`               | ADMIN                      |
| `POST /officers/**`                | ADMIN                      |
| `PATCH /accounts/*/status`         | ADMIN                      |
| `POST /accounts/deceased`          | Internal (X-Life-Event-Key)|
| `POST /contributions`              | OFFICER or EMPLOYER        |
| `PATCH /contributions/*/verify`    | OFFICER                    |
| `POST /retirement/**`              | OFFICER                    |
| Everything else                    | OFFICER or EMPLOYER        |

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
| `BRIDGE_INSTITUTION_KEY`  | API key for INST_PENSION in bridge               |
| `NOTIFICATION_URL`        | Base URL of sanly-notifications                  |
| `NOTIFICATION_SERVICE_KEY`| Service key for SANLY_PENSION notifications      |
| `LIFE_EVENT_SERVICE_KEY`  | Shared key for civil→pension death events        |

---

## Quick Start (standalone)

```bash
docker-compose up -d

curl -X POST http://localhost:8099/api/v1/pension/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
