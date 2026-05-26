# sanly-documents

**Port:** 8102 | **DB Port:** 5451 | **DB:** sanly_documents

Document Wallet and Status Tracker for the SANLY e-governance platform.

---

## Purpose

### 1. Document Wallet
Citizens request certified digital PDF copies of government documents (diplomas, birth certificates, driving licenses, etc.). The service:
- Fetches raw record data from the issuing source service
- Generates a tamper-evident SHA-256 hash sealing the certificate fields
- Renders a signed A4 PDF with a QR code pointing to the public verification URL
- Stores only the field data (JSON), not the PDF bytes — PDF is regenerated on each download

### 2. Status Tracker
Provides cross-service application tracking. Any back-end service can push a status update via `POST /api/v1/documents/tracking/update` (service-key auth). Citizens can then check the live status of any application through their portal.

---

## Certificate Generation Flow

```
Citizen                    sanly-documents              Source Service         Registry
  |                              |                            |                    |
  |-- POST /certificates/generate -->                         |                    |
  |   { documentType, sourceRecordCode }                      |                    |
  |                              |-- GET /diplomas/{code} -->  |                   |
  |                              |<-- { fieldData } ----------|                   |
  |                              |-- GET /citizens/{id}/verify -----------------> |
  |                              |<-- { fullName } --------------------------------|
  |                              |                                                 |
  |                              | [generate certCode TM-CERT-YYYYNNNNNN]          |
  |                              | [compute SHA-256 hash]                          |
  |                              | [save to DB]                                    |
  |                              | [render PDF with PDFBox + QR code]              |
  |                              | [async: notify CERTIFICATE_GENERATED]           |
  |<-- 200 application/pdf ------|                                                 |
```

---

## PDF Structure

Each generated PDF (A4) contains:

1. **Header** — "SANLY — Digital Government of Turkmenistan" in dark blue
2. **Subtitle** — "CERTIFIED DIGITAL DOCUMENT"
3. **Document type** — ALL CAPS label
4. **Certificate title** — human-readable document name
5. **Horizontal separator**
6. **Holder info** — name + masked national ID
7. **Field table** — key-value pairs sourced from the issuing service
8. **Certificate code box** — `TM-CERT-YYYYNNNNNN` in a highlighted box
9. **Dates** — issued at, valid until
10. **QR code** (bottom right, 80x80pt) — encodes the public verify URL
11. **Footer** — verification URL + hash fingerprint
12. **Diagonal watermark** — "CERTIFIED DIGITAL COPY" in light gray at 45 degrees

Fonts: PDFBox built-in Type1 (Helvetica, Helvetica-Bold, Courier-Bold). No external font files required.

---

## Hash Verification

The verification hash is computed as:

```
SHA-256(certCode + "|" + nationalId + "|" + docType + "|" + sourceCode + "|" + issuedAt + "|" + SERVER_CERT_SECRET)
```

This hash is stored in the DB, printed in the PDF footer, and re-verified on every `GET /verify/{certCode}` call. Any tampering with the certificate fields causes the hash check to fail.

---

## Status Tracker Integration

Other services push updates to the tracker via a simple HTTP call:

```http
POST /api/v1/documents/tracking/update
X-Service-Key: <TRACKING_SERVICE_KEY>
Content-Type: application/json

{
  "citizenNationalId": "12345678901",
  "itemType": "LICENSE_APPLICATION",
  "sourceService": "DMV",
  "sourceItemCode": "LIC-2026-001234",
  "title": "Driving License Application",
  "currentStatus": "UNDER_REVIEW",
  "statusDescription": "Your documents are being verified by an officer.",
  "isCompleted": false
}
```

- First call with a given `sourceItemCode` creates a new `TrackedItem` with a `TM-TRK-YYYYNNNNNN` code.
- Subsequent calls update the status in place and append a `TrackingUpdate` record.
- When `isCompleted: true`, the item is marked complete and the citizen receives a push notification.

---

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_PASSWORD` | Yes | — | PostgreSQL password |
| `JWT_SECRET` | Yes | — | HMAC-SHA256 key (min 32 chars) |
| `SERVER_CERT_SECRET` | Yes | — | Secret mixed into certificate hash |
| `ADMIN_PASSWORD` | Yes | — | Password for seeded admin account |
| `REGISTRY_INSTITUTION_TOKEN` | Yes | — | Bearer token for registry citizen lookups |
| `ADMIN_USERNAME` | No | `admin` | Admin username |
| `DB_URL` | No | `jdbc:postgresql://localhost:5451/sanly_documents` | Full JDBC URL |
| `DB_USERNAME` | No | `documents_user` | DB username |
| `CIVIL_SERVICE_TOKEN` | Yes | — | Bearer token for civil registry calls |
| `DMV_SERVICE_TOKEN` | Yes | — | Bearer token for DMV calls |
| `EDUCATION_SERVICE_TOKEN` | Yes | — | Bearer token for education service calls |
| `LAND_SERVICE_TOKEN` | Yes | — | Bearer token for land registry calls |
| `BUSINESS_SERVICE_TOKEN` | Yes | — | Bearer token for business registry calls |
| `TAX_SERVICE_TOKEN` | Yes | — | Bearer token for tax service calls |
| `POLICE_SERVICE_TOKEN` | Yes | — | Bearer token for police records calls |
| `NOTIFICATION_SERVICE_URL` | No | `http://localhost:8088` | Notification service base URL |
| `NOTIFICATIONS_KEY_DOCUMENTS` | No | `documents-default-key-change-me` | Service key for notifications |
| `TRACKING_SERVICE_KEY` | No | `tracking-internal-key-change-me` | Key for inbound tracking updates |
| `QR_BASE_URL` | No | `https://sanly.tm/verify-cert` | Base URL embedded in QR codes |
| `JWT_EXPIRATION_MS` | No | `86400000` | JWT TTL in ms (default 24h) |

---

## API Summary

### Auth
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/documents/auth/login` | None | Admin login → JWT |

### Document Wallet (Citizen)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/documents/certificates/generate` | CITIZEN JWT | Generate + download PDF |
| GET | `/api/v1/documents/certificates/my` | CITIZEN JWT | List my certificates |
| GET | `/api/v1/documents/certificates/{certCode}` | CITIZEN/ADMIN JWT | Get metadata |
| GET | `/api/v1/documents/certificates/{certCode}/download` | CITIZEN/ADMIN JWT | Re-download PDF |
| DELETE | `/api/v1/documents/certificates/{certCode}` | CITIZEN/ADMIN JWT | Revoke |

### Verification (Public)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/documents/verify/{certCode}` | None | Verify by code |
| POST | `/api/v1/documents/verify` | None | Verify via form param |

### Status Tracker
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/documents/tracking/update` | X-Service-Key | Push status update (internal) |
| GET | `/api/v1/documents/tracking/my` | CITIZEN JWT | My tracked items |
| GET | `/api/v1/documents/tracking/{trackingCode}` | CITIZEN JWT | Full timeline |
| GET | `/api/v1/documents/tracking/search?code=` | CITIZEN JWT | Search by source code |

### Admin
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/documents/admin/all` | ADMIN JWT | All certificates (paginated) |
| PATCH | `/api/v1/documents/admin/{certCode}/revoke` | ADMIN JWT | Revoke any certificate |
| GET | `/api/v1/documents/admin/stats` | ADMIN JWT | Dashboard statistics |
| GET | `/api/v1/documents/admin/tracking` | ADMIN JWT | All tracked items (paginated) |

### Swagger UI
Available at `http://localhost:8102/swagger-ui.html`
