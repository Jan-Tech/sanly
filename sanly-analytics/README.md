# SANLY Analytics Service

Government reporting and analytics platform for the SANLY e-governance system.
Port **8103** | Database **sanly_analytics** (port 5452)

## Purpose

Provides aggregated, anonymized statistics about Turkmenistan for government ministers, World Bank officials, and UN representatives. **No individual citizen data is exposed** — only counts, percentages, and trends.

## Data Collection Architecture

### Push Model (Primary)
Each source service has an `AnalyticsClient` that pushes daily stats via `POST /api/v1/analytics/ingest/daily`. This is `@Async` — it never blocks the calling service's transaction. Any failure is silently logged.

```
sanly-appointments ──→ POST /ingest/daily  { appointmentsToday: 45, noShowCount: 3, ... }
sanly-business     ──→ POST /ingest/daily  { newBusinessesToday: 2, activeBusinesses: 1847, ... }
citizen-registry   ──→ POST /ingest/daily  { totalCitizens: 6_000_000, newRegistrationsToday: 42, ... }
sanly-bridge       ──→ POST /ingest/anomaly { institutionCode: "INST_POLICE", alertType: "BULK_ACCESS", alertCount: 3, ... }
```

Multiple services push partial data throughout the day. The snapshot upsert merges non-null fields, so a citizen-registry push and a business push combine into a single `DailySnapshot` for that date.

### Scheduled Collection (Secondary)
- **23:30 daily** — `DailySnapshotScheduler` finalizes the day's snapshot and fires `DAILY_REPORT_READY` notification to admins.
- **Monday 07:00** — `WeeklyReportScheduler` auto-generates a `FULL_GOVERNMENT_REPORT` for the past 7 days.

## Report Types

| Type | Contents |
|------|----------|
| `POPULATION_SUMMARY` | Total/active citizens, growth rate, regional distribution |
| `BUSINESS_ACTIVITY` | Business formation, active count by status |
| `SERVICE_USAGE` | Endpoint request counts, error rates, response times |
| `ANTI_CORRUPTION_METRICS` | Anomaly alerts by type and institution, resolution rates |
| `REGIONAL_BREAKDOWN` | Citizens/businesses/properties/appointments by region |
| `ECONOMIC_INDICATORS` | Business formation rate, property transactions, customs clearances |
| `PLATFORM_HEALTH` | Service health, error rates, exchange volumes |
| `FULL_GOVERNMENT_REPORT` | All of the above in one document |
| `AUDIT_LOG` | Bridge exchange audit with institution details |

All reports are generated as PDF (PDFBox 3.x) and CSV (OpenCSV). PDFs are stored as BYTEA in the database and expire after 24 hours.

## KPI Definitions

### Population KPIs
- **Growth Rate (30d)**: `(today.totalCitizens - 30d_ago.totalCitizens) / 30d_ago.totalCitizens × 100`
- **Gender Ratio**: Approximated at 50/50 (privacy by design — no NIN queries in analytics)

### Economy KPIs
- **Business Formation Rate**: `sum(newBusinessesToday) / 30 days`
- **Property Transfer Volume**: `sum(transfersToday) / 30 days`

### Service KPIs
- **No-Show Rate**: `noShowCount / appointmentsToday × 100`
- **Avg Appointment Rating**: Weighted average pushed by appointments service

### Anti-Corruption Metrics
- **Anomaly Resolution Rate**: `sum(resolvedCount) / sum(alertCount) × 100`
- **Top Flagged Institutions**: Ranked by total alert count over last 30 days
- **Alert Types**: BULK_ACCESS, UNAUTHORIZED_QUERY, SUSPICIOUS_PATTERN, REPEATED_FAILURE

## Setup

1. Copy `.env.example` to `.env` in root `sanly/` folder, add:
   ```
   ANALYTICS_DB_USER=analytics_user
   ANALYTICS_DB_PASSWORD=<strong-password>
   ANALYTICS_ADMIN_PASSWORD=<strong-password>
   ANALYTICS_SERVICE_KEY=<min-32-chars-random>
   NOTIFICATIONS_KEY_ANALYTICS=<random-key>
   ```

2. Start with `docker-compose up --build`

3. Login: `POST http://localhost:8080/api/v1/analytics/auth/login`
   ```json
   { "username": "admin", "password": "<ANALYTICS_ADMIN_PASSWORD>" }
   ```

4. Use JWT for all analytics admin endpoints.

## Ingest Endpoint (Service-to-Service)

Other services push stats with their `ANALYTICS_SERVICE_KEY`:
```
POST /api/v1/analytics/ingest/daily
X-Service-Key: <ANALYTICS_SERVICE_KEY>

{
  "serviceName": "CITIZEN_REGISTRY",
  "totalCitizens": 5982134,
  "activeCitizens": 5841200,
  "newRegistrationsToday": 37
}
```

Only non-null fields are written — partial pushes are safe.

## Security

- All analytics endpoints require `ROLE_ADMIN` JWT
- Ingest endpoints validate `X-Service-Key` header (not JWT)
- No individual citizen records ever leave the source services
- Report downloads expire after 24 hours
- Rate limit: 10 report generation requests/hour per IP
