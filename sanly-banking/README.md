# SANLY Banking API Service

Regulated API integration that allows licensed financial institutions to verify citizen identity and data **with explicit citizen consent via SMS OTP**. Modelled after Singapore's MyInfo and Estonia's X-Road banking integration.

Port **8104** | Database **sanly_banking** (port 5453)

---

## Consent Flow

```
┌─────────┐                        ┌──────────────┐                     ┌──────────────────┐
│  Bank   │                        │  SANLY Portal │                     │ sanly-banking API │
└────┬────┘                        └──────┬────────┘                     └────────┬─────────┘
     │                                    │                                        │
     │  POST /consent/request             │                                        │
     │  X-Bank-Code: TM-BNK-001          │                                        │
     │  X-Bank-Key: <api-key>            │                                        │
     │  { citizenNationalId, scopes, purpose }                                    │
     │───────────────────────────────────────────────────────────────────────────▶│
     │                                    │                                        │
     │◀ ─ ─ { consentCode, expiresIn:600 }─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│
     │                                    │                                        │
     │                                    │◀─ ─ SMS OTP sent to citizen ─ ─ ─ ─ ─│
     │                                    │                                        │
     │  GET /consent/{consentCode}/status │                                        │
     │  (polls every 10 seconds)          │                                        │
     │───────────────────────────────────────────────────────────────────────────▶│
     │◀ ─ ─ { status: PENDING } ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│
     │                                    │                                        │
     │                    Citizen sees consent request in portal                   │
     │                    and selects scopes to approve                            │
     │                                    │                                        │
     │                                    │  POST /consent/{code}/approve          │
     │                                    │  { approvedScopes, otpCode }           │
     │                                    │───────────────────────────────────────▶│
     │                                    │◀ ─ 200 OK ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│
     │                                    │                                        │
     │  GET /consent/{consentCode}/status │                                        │
     │───────────────────────────────────────────────────────────────────────────▶│
     │◀ ─ ─ { status: APPROVED, consentToken: "abc123..." } ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│
     │                                    │       (token cleared from DB)          │
     │                                    │                                        │
     │  GET /citizen/profile              │                                        │
     │  X-Bank-Code: TM-BNK-001          │                                        │
     │  X-Bank-Key: <api-key>            │                                        │
     │  X-Consent-Token: abc123...        │                                        │
     │───────────────────────────────────────────────────────────────────────────▶│
     │                                    │        Token marked USED               │
     │                                    │        Data fetched from services      │
     │◀ ─ ─ { data: { identity, taxStatus, criminalClearance, ... } } ─ ─ ─ ─ ─ ─│
     │                                    │                                        │
     │                                    │◀─ ─ BANKING_DATA_ACCESSED notification│
```

---

## Consent Scopes

| Scope | Plain-Language Description |
|-------|---------------------------|
| `IDENTITY_BASIC` | Name, date of birth, masked national ID |
| `IDENTITY_FULL` | Full name, DOB, address, masked phone |
| `TAX_STATUS` | Whether taxes are up-to-date (yes/no) |
| `TAX_INCOME_CLASS` | Income bracket: LOW / MEDIUM / HIGH (not exact salary) |
| `CRIMINAL_CLEARANCE` | CLEAR or HAS_RECORD (no details shared) |
| `BUSINESS_OWNERSHIP` | List of businesses registered under citizen's name |
| `PROPERTY_OWNERSHIP` | Property count and total value range |
| `PENSION_STATUS` | Pension account status and estimated monthly range |
| `EMPLOYMENT_STATUS` | Whether citizen has active pension contributions |
| `MEDICAL_CLEARANCE` | CLEARED or NOT_CLEARED (no medical details) |
| `DRIVING_LICENSE` | License status, categories, expiry date |

**Privacy guarantees**: Criminal records return only `CLEAR`/`HAS_RECORD`. Property values return ranges (`LOW_VALUE` / `MEDIUM_VALUE` / `HIGH_VALUE`), never exact amounts. Income returns brackets, never exact figures. Medical returns only cleared/not-cleared status.

---

## Anti-Money Laundering (AML) Support

Banks can use the Banking API to automate AML compliance checks:

1. **Identity Verification** — `IDENTITY_FULL` confirms citizen's real identity without branch visit
2. **Source of Funds** — `BUSINESS_OWNERSHIP` shows registered businesses (legitimate income sources)
3. **Tax Compliance** — `TAX_STATUS` confirms citizen is tax-compliant (red flag if not)
4. **Criminal Check** — `CRIMINAL_CLEARANCE` reveals prior convictions without exposing details
5. **Property Ownership** — `PROPERTY_OWNERSHIP` helps assess wealth against stated income

Every access is permanently logged and visible to the citizen in the SANLY portal — creating a transparent audit trail that discourages fraudulent use of the API.

---

## Comparison: SANLY Banking API vs Singapore MyInfo

| Feature | SANLY Banking API | Singapore MyInfo |
|---------|------------------|-----------------|
| Consent mechanism | SMS OTP, citizen selects scopes | SingPass login |
| Scope granularity | 11 data scopes | 20+ attributes |
| Token lifetime | 1 hour, single-use | 1 hour |
| Citizen visibility | Full access history in portal | Limited |
| Criminal data | CLEAR/HAS_RECORD only | Not included |
| Property data | Count + value range | Not included |
| Consent expiry | 10 minutes | Session-based |
| Partial approval | Yes — citizen can approve subset of scopes | No |

---

## Setup

1. Add to root `.env`:
   ```
   BANKING_DB_USER=banking_user
   BANKING_DB_PASSWORD=<strong-password>
   BANKING_ENCRYPTION_KEY=<exactly-32-chars>
   BANKING_ADMIN_PASSWORD=<strong-password>
   BANKING_REGISTRY_SERVICE_KEY=<random-32-chars>
   NOTIFICATIONS_KEY_BANKING=<random-key>
   ```

2. Start: `docker-compose up --build`

3. Login as admin: `POST http://localhost:8080/api/v1/banking/auth/login`

4. Register a bank: `POST http://localhost:8080/api/v1/banking/banks`
   - Returns a one-time API key — save it immediately
   - Approve the bank: `PATCH /api/v1/banking/banks/{bankCode}/approve`

---

## Security Properties

- **Bank API keys** — BCrypt(12) hashed, never stored in plaintext
- **Consent tokens** — SHA-256 hashed, single-use, 1-hour TTL
- **OTP required** — Citizens must confirm via phone OTP; cannot be bypassed
- **Consent expires in 10 minutes** — Prevents replay attacks
- **All accesses permanent** — `BankDataAccess` records cannot be deleted
- **Rate limiting** — 20 consent requests per bank per hour (Bucket4j)
- **Partial approval** — Citizens may deselect individual scopes
- **Privacy by design** — Sensitive fields return categories, not raw values
