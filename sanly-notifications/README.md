# sanly-notifications — Citizen Notification Service

Real-time alert system for the SANLY e-government platform. Citizens receive notifications whenever something significant happens to their government data — they should never be surprised by changes to their records.

**Port:** 8088  
**Auth:** Service-to-service via `X-Service-Key` header; citizen/admin via Bearer JWT (citizen-registry JWT secret)

---

## Architecture

```
Other SANLY services
     │  POST /api/v1/notifications/send
     │  X-Service-Name: SANLY_BRIDGE
     │  X-Service-Key: <bcrypt-verified key>
     ▼
sanly-notifications
     ├── Looks up template by eventType + language
     ├── Fills {variable} placeholders from metadata
     ├── Saves Notification (UNREAD)
     └── Async: email delivery (if configured)
          ▼
     db-notifications (PostgreSQL, port 5440)
```

Citizens poll `GET /api/v1/notifications/my/unread-count` every 60 s from the portal to drive the notification bell badge.

---

## API Reference

### Service-to-service (X-Service-Key auth)

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/notifications/send` | Send one notification |
| POST | `/api/v1/notifications/send-bulk` | Send to multiple citizens (e.g. both spouses on marriage) |

**Request body for `/send`:**
```json
{
  "citizenNationalId": "12345678901",
  "eventType": "DRIVING_LICENSE_ISSUED",
  "language": "EN",
  "metadata": {
    "licenseNumber": "TM-DL-2024000001",
    "category": "B",
    "issuedAt": "2024-01-15",
    "expiresAt": "2034-01-15"
  }
}
```

### Citizen endpoints (Bearer JWT)

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/notifications/my` | Own notifications, paginated. Filter: `?status=UNREAD&eventType=...` |
| GET | `/api/v1/notifications/my/unread-count` | `{ "count": 5 }` — portal polls this |
| GET | `/api/v1/notifications/my/recent` | Last 5 notifications (for bell dropdown) |
| PATCH | `/api/v1/notifications/my/{id}/read` | Mark one as read |
| PATCH | `/api/v1/notifications/my/read-all` | Mark all as read |
| GET | `/api/v1/notifications/my/preferences` | Channel preferences per event type |
| PUT | `/api/v1/notifications/my/preferences` | Update preferences |

### Admin endpoints (ROLE_ADMIN)

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/notifications/admin/all` | All notifications, filterable |
| GET | `/api/v1/notifications/admin/stats` | Delivery stats (total sent, failed, by event type) |
| GET | `/api/v1/notifications/admin/templates` | List all active templates |
| PUT | `/api/v1/notifications/admin/templates/{id}` | Edit template text |

---

## Event Types

| Category | Event | When fired |
|---|---|---|
| Data access | `DATA_ACCESSED_BY_POLICE` | Bridge: successful police query |
| Data access | `DATA_ACCESSED_BY_TAX` | Bridge: successful tax query |
| Data access | `DATA_ACCESSED_BY_DMV` | Bridge: successful DMV query |
| Data access | `DATA_ACCESSED_BY_MEDICAL` | Bridge: successful medical query |
| Data access | `DATA_ACCESSED_BY_BUSINESS` | Bridge: successful business query |
| Record change | `DRIVING_LICENSE_ISSUED` | DMV: license approved and issued |
| Record change | `DRIVING_LICENSE_SUSPENDED` | DMV: license suspended |
| Record change | `MEDICAL_RECORD_ADDED` | Medical: new test result saved |
| Record change | `TAX_STATUS_CHANGED` | Tax: filing processed |
| Record change | `BUSINESS_REGISTRATION_APPROVED` | Business: application approved |
| Record change | `BUSINESS_REGISTRATION_REJECTED` | Business: application rejected |
| Record change | `CRIMINAL_RECORD_ADDED` | Police: criminal record saved |
| Record change | `CIVIL_BIRTH_REGISTERED` | Civil: birth certificate issued |
| Record change | `CIVIL_MARRIAGE_REGISTERED` | Civil: marriage registered |
| Record change | `CIVIL_DEATH_REGISTERED` | Civil: death registered |
| Security | `ANOMALY_DETECTED_ON_YOUR_DATA` | Bridge anomaly detection: alert with citizen NIN |
| Security | `SUSPICIOUS_ACCESS_DETECTED` | Future use |
| Account | `CITIZEN_STATUS_CHANGED` | Registry: status updated |
| Account | `PASSWORD_CHANGED` | Registry: password changed |

---

## Template System

Templates are stored in PostgreSQL (`notification_templates`) with a unique key of `(event_type, language, channel)`. Variables use `{variableName}` syntax — replaced at send time with values from the `metadata` map.

Seed templates are in `V5__seed_templates.sql` covering all events above in EN/RU/TK. Admins can edit template text via the admin API without redeploying.

**Variable sanitization:** All metadata values have HTML tags stripped before injection, preventing XSS in notification bodies.

---

## Service Account Setup

Each calling service has a service account pre-seeded with a default key in `DataInitializer`. **Change these keys before production:**

1. Update the `key_hash` column in `service_accounts` with a BCrypt hash of the new key.
2. Set the matching plaintext key in each caller's `NOTIFICATION_SERVICE_KEY` env var.

Or call the service with the default key and update via the database directly.

---

## Email Configuration

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}  # Gmail: use App Password, not account password
```

For Gmail, enable 2FA and generate an App Password at myaccount.google.com/apppasswords.

Email delivery is `@Async` — API responses return immediately. Failed deliveries mark the notification as `FAILED` and log the error.

---

## SMS

`SmsDeliveryService` is a stub that logs message content. Replace the log statement with a real SMS provider (Twilio, local Turkmenistan carrier, etc.) when ready.

---

## Docker

```bash
# Standalone
cd sanly-notifications
docker-compose up --build

# Full platform (root)
make up
```
