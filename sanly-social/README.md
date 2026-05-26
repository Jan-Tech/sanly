# SANLY Social — Social Benefits, Pensions & Unemployment Authority

Port: **8092** | Database: `sanly_social` (port 5444) | Institution code: `INST_SOCIAL`

## Purpose

Manages all social benefit programs, citizen claims, monthly payments, pension accounts, and unemployment registrations for the SANLY e-governance platform of Turkmenistan.

---

## Benefit Types

| Type | Description |
|------|-------------|
| `CHILD_BENEFIT` | Auto-triggered on child birth for mother |
| `MATERNITY` | Maternity leave benefit |
| `PATERNITY` | Paternity leave benefit |
| `DISABILITY` | Disability support benefit |
| `UNEMPLOYMENT` | Auto-triggered on unemployment registration |
| `PENSION` | Auto-triggered when pension account reaches PAYING status |
| `SURVIVOR` | Auto-triggered on spouse's death registration |
| `HOUSING` | Housing support benefit |
| `EDUCATION_GRANT` | Education grant for students |
| `LOW_INCOME` | Low-income support |

---

## Life Event Triggers

| Life Event | Trigger | Endpoint Called |
|------------|---------|----------------|
| Birth registered | `CHILD_BENEFIT` for mother | `POST /api/v1/social/claims/auto-trigger` |
| Death registered | Cancel all benefits for deceased | `POST /api/v1/social/claims/cancel-all` |
| Death registered | `SURVIVOR` benefit for surviving spouse | `POST /api/v1/social/claims/auto-trigger` |
| Marriage registered | `MARRIAGE_BENEFIT_INFO` notification to both spouses | (via notification service) |
| Unemployment registered | `UNEMPLOYMENT` benefit | Internal trigger within UnemploymentService |

All life-event endpoints are protected with `X-Life-Event-Key` header (set by `sanly-civil`).

---

## Scheduled Jobs

| Job | Schedule | Action |
|-----|----------|--------|
| `MonthlyPaymentScheduler` | 1st of every month, 08:00 | Creates SCHEDULED payment for each ACTIVE claim; sends `BENEFIT_PAYMENT_SCHEDULED` notification |
| `PensionEligibilityChecker` | Daily at 07:00 | Finds ACCUMULATING pensions past their `eligibleAt` date; sets to ELIGIBLE; sends `PENSION_ELIGIBLE` notification |
| `UnemploymentExpiry` | Daily at 06:00 | Finds REGISTERED unemployment records older than 12 months; sets to EXPIRED; suspends linked claim; sends `UNEMPLOYMENT_BENEFIT_EXPIRED` notification |

---

## Pension Eligibility Calculation

Pension `eligibleAt` is calculated at account creation using the gender digit from the citizen's National ID:

- **NIN position 6** (0-indexed): odd digit → male (25 year contribution requirement)
- **NIN position 6**: even digit → female (20 year contribution requirement)

`eligibleAt = contributionStartDate + retirementYears`

---

## Tax Compliance Check

Before approving a manually submitted claim (`CITIZEN_APPLIED` or `OFFICER_INITIATED`), the service queries `SANLY Bridge` for `TAX_STATUS`. If the citizen is `NON_COMPLIANT`, approval is blocked with HTTP 409.

Auto-triggered claims (`AUTO_TRIGGERED`) bypass the tax check — life events like birth or death are unconditionally eligible.

---

## API Overview

| Resource | Endpoint |
|----------|----------|
| Auth | `POST /api/v1/social/auth/login` |
| Programs | `GET/POST /api/v1/social/programs`, `PATCH /{code}/status` (ADMIN) |
| Claims | `POST /api/v1/social/claims`, `GET /citizen/{nin}`, `POST /{code}/approve|reject|suspend|reinstate` |
| Auto-trigger | `POST /api/v1/social/claims/auto-trigger` (Life-event key) |
| Cancel all | `POST /api/v1/social/claims/cancel-all` (Life-event key) |
| Payments | `GET /api/v1/social/payments/claim/{code}`, `/citizen/{nin}`, `/scheduled`, `PATCH /{id}/mark-paid` |
| Unemployment | `POST /api/v1/social/unemployment`, `GET /{nin}`, `PATCH /{nin}/mark-employed` |
| Pensions | `POST /api/v1/social/pensions`, `GET /{nin}`, `PATCH /{nin}/contribute`, `GET /{nin}/eligibility`, `POST /{nin}/start-paying` |
| Officers | `POST/GET /api/v1/social/officers`, `PATCH /{id}/status` (ADMIN) |

---

## Encrypted Fields

All sensitive financial data is encrypted at rest using AES-256-GCM:

- `benefit_programs.monthly_amount`
- `benefit_payments.amount`, `.notes`
- `benefit_claims.notes`
- `unemployment_records.last_employer`
- `pension_accounts.total_contributions`

Requires `ENCRYPTION_KEY` = exactly 32 chars.

---

## Local Development

```bash
# Copy and fill environment
cp .env.example .env

# Run standalone with Docker
docker-compose up --build

# Swagger UI
open http://localhost:8092/swagger-ui.html
```

---

## Notification Event Types

| Event | When Sent |
|-------|-----------|
| `BENEFIT_CLAIM_APPROVED` | Officer manually approves a claim |
| `BENEFIT_CLAIM_REJECTED` | Officer rejects a claim |
| `BENEFIT_AUTO_TRIGGERED` | Life event creates an auto-approved claim |
| `BENEFIT_PAYMENT_SCHEDULED` | Monthly scheduler creates a new payment |
| `PENSION_ELIGIBLE` | Pension account crosses its `eligibleAt` date |
| `UNEMPLOYMENT_BENEFIT_EXPIRED` | 12-month unemployment registration expires |
| `MARRIAGE_BENEFIT_INFO` | Marriage registered (sent to both spouses) |
