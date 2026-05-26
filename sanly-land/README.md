# sanly-land

Land Registry for the SANLY e-governance platform. Port **8091**.

## Purpose

Officers register properties, record ownership, and process transfers between citizens. Every property in Turkmenistan is linked to a citizen's TM-NIN. Buying, selling, or inheriting property is done digitally — no paper deed, no notary visit, no corruption. Banks verify property ownership before issuing mortgages.

## Entities

| Entity | Description |
|---|---|
| `LandOfficer` | Officers and admins who manage properties |
| `Property` | Registered property (`TM-CAD-YYYYNNNNNN`) |
| `PropertySequence` | Pessimistic-locked per-year sequence for cadastral numbers |
| `Ownership` | Links a citizen to a property with share percentage |
| `TransferApplication` | Transfer request (SALE / GIFT / INHERITANCE / COURT_ORDER) |
| `PropertyValuation` | Encrypted valuation records for tax / mortgage assessment |

**Encrypted fields**: `address`, `description` (Property); `agreedPrice`, `notes` (TransferApplication); `valuationAmount` (PropertyValuation) — all AES-256-GCM.

## Endpoints

### Auth
| Method | Path | Auth |
|---|---|---|
| `POST` | `/api/v1/land/auth/login` | Public |

### Properties
| Method | Path | Auth | Notes |
|---|---|---|---|
| `POST` | `/api/v1/land/properties` | OFFICER | Generates TM-CAD code |
| `GET` | `/api/v1/land/properties/{code}` | Authenticated | Returns property + owners |
| `GET` | `/api/v1/land/properties/owner/{nin}` | Authenticated | All ACTIVE ownerships |
| `GET` | `/api/v1/land/properties/verify/{code}` | **Public** | For banks / notaries |
| `PATCH` | `/api/v1/land/properties/{code}/status` | ADMIN | |

### Ownership
| Method | Path | Notes |
|---|---|---|
| `POST` | `/api/v1/land/ownership` | Assign initial owner |
| `GET` | `/api/v1/land/ownership/property/{code}` | Active owners |
| `GET` | `/api/v1/land/ownership/citizen/{nin}` | Citizen's holdings |
| `POST` | `/api/v1/land/ownership/deceased` | Internal — X-Life-Event-Key |

### Transfers
| Method | Path | Notes |
|---|---|---|
| `POST` | `/api/v1/land/transfers` | Verifies both parties, locks property |
| `GET` | `/api/v1/land/transfers/{id}` | |
| `GET` | `/api/v1/land/transfers/property/{code}` | Full history |
| `GET` | `/api/v1/land/transfers/citizen/{nin}` | Sent + received |
| `GET` | `/api/v1/land/transfers/pending` | Officer work queue |
| `POST` | `/api/v1/land/transfers/{id}/approve` | Atomic + tax compliance check |
| `POST` | `/api/v1/land/transfers/{id}/reject` | With reason |

### Valuations / Officers
Standard OFFICER/ADMIN CRUD — see Swagger.

## Transfer approval flow

```
Officer → POST /transfers/{id}/approve
          │
          ├─ Pessimistic WRITE lock on TransferApplication
          ├─ Pessimistic WRITE lock on Property
          │
          ├─ Bridge query: GET /exchange/data/{sellerNin}/TAX_STATUS
          │    └── if NON_COMPLIANT → 409 CONFLICT
          │         "Transfer cannot be approved: seller has outstanding tax obligations."
          │
          ├─ Set old Ownership records → TRANSFERRED
          ├─ Create new Ownership record for buyer
          ├─ Set Property.status → REGISTERED
          ├─ Async publish PROPERTY_RECORD to bridge
          └─ Notify both parties via sanly-notifications
```

**Race condition protection**: Both the TransferApplication and Property are locked with `@Lock(PESSIMISTIC_WRITE)`. If two officers try to approve the same transfer simultaneously, one will wait and then fail (status is no longer PENDING).

## Anti-corruption features

- **Tax compliance check**: seller must be COMPLIANT before any transfer is approved
- **Full audit trail**: every transfer, approval, rejection permanently recorded
- **Bridge publication**: all ownership changes published to SANLY Bridge — queryable by banks (`dataType=PROPERTY_RECORD`)
- **Public verification**: `GET /api/v1/land/properties/verify/{code}` — no auth, banks and notaries can verify ownership without creating an account

## Life-events integration

On **death registration** (sanly-civil), `LandServiceClient` calls `POST /api/v1/land/ownership/deceased`:
- All ACTIVE ownerships for the deceased → status INHERITED
- Property status → UNDER_TRANSFER
- Co-owners notified: `PROPERTY_INHERITANCE_PENDING`

## Environment variables

| Variable | Description |
|---|---|
| `JWT_SECRET` | Min 32 chars |
| `ENCRYPTION_KEY` | Exactly 32 chars (AES-256-GCM) |
| `ADMIN_PASSWORD` | Seed admin password |
| `CITIZEN_REGISTRY_URL` | For verifying parties before transfers |
| `CITIZEN_REGISTRY_TOKEN` | ROLE_INSTITUTION JWT |
| `BRIDGE_URL` | SANLY Bridge URL |
| `BRIDGE_INSTITUTION_KEY` | API key for INST_LAND |
| `NOTIFICATION_URL` | sanly-notifications URL |
| `NOTIFICATION_SERVICE_KEY` | Service auth key |
| `LIFE_EVENT_SERVICE_KEY` | Shared key for civil registry calls |

## Setup (full platform)

```bash
# 1. Start the platform
make up

# 2. Log in as land admin (via gateway)
POST http://localhost:8080/land/api/v1/land/auth/login
{"username":"admin","password":"<LAND_ADMIN_PASSWORD>"}

# 3. Register INST_LAND on sanly-bridge
POST http://localhost:8080/bridge/api/v1/institutions
{"name":"SANLY Land Registry","code":"INST_LAND"}
# Grant permission: INST_LAND → INST_TAX → TAX_STATUS (for transfer approval)

# 4. Register a property
POST http://localhost:8080/land/api/v1/land/properties
{"propertyType":"RESIDENTIAL_APARTMENT","address":"Magtymguly 1","region":"Aşgabat","area":75}

# 5. Assign initial ownership
POST http://localhost:8080/land/api/v1/land/ownership
{"cadastralNumber":"TM-CAD-2024000001","ownerNationalId":"...","ownershipShare":100,
 "ownershipType":"SOLE","acquiredAt":"2024-01-01","acquiredVia":"PURCHASE"}
```

## Swagger UI

`http://localhost:8080/land/swagger-ui.html` (via gateway)
