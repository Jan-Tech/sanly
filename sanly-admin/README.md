# sanly-admin — Government Administration Dashboard

Internal React dashboard for government administrators. Provides a unified view across all eight SANLY backend services: citizen registry, inter-agency bridge, medical, DMV, police, tax, business, and civil registry.

**Port:** 5174 (dev server) / 5174 (Docker)  
**Auth:** ROLE_ADMIN JWT from citizen-registry (POST /api/v1/auth/login)

---

## Pages

| Route | Page | Description |
|---|---|---|
| `/` | Dashboard | Platform health cards, exchange volume chart (24h), open anomalies, recent registrations |
| `/citizens` | Citizens | Paginated/searchable citizen table with status filter |
| `/citizens/:nin` | Citizen Detail | Full citizen profile: identity, medical, DMV, tax, business, civil, police, access log |
| `/institutions` | Institutions | Register, activate, suspend, rotate API keys |
| `/permissions` | Permissions | Grant/revoke inter-agency data access; table + matrix views |
| `/audit` | Audit Log | Full bridge exchange log: 7 filters, CSV export, detail modal |
| `/anomalies` | Anomalies | Tabbed OPEN/REVIEWED/DISMISSED; review modal with reviewer attribution |
| `/health` | Health | Live service status, latency, Swagger links; auto-refreshes every 15 s |
| `/settings` | Settings | Change admin password; data-type and purpose-code reference tables |

---

## Setup

### Development

```bash
cp .env.example .env        # in repo root — fill ADMIN_* tokens
npm install
npm run dev
```

Vite proxies all `/proxy/*` calls to Docker-internal hostnames — no CORS configuration needed.

### Docker (via root Makefile)

```bash
# From repo root:
make up
# sanly-admin starts after citizen-registry and sanly-bridge are healthy
```

Access at http://localhost:5174

---

## Environment Variables

Set in root `.env` (not this directory). Referenced through `docker-compose.yml` → container environment → Vite `VITE_*` build vars.

| Variable | Description |
|---|---|
| `ADMIN_BRIDGE_TOKEN` | Bridge admin JWT (POST /api/v1/auth/login on sanly-bridge) |
| `ADMIN_POLICE_TOKEN` | Police service JWT (for citizen detail police section) |
| `REGISTRY_INSTITUTION_TOKEN` | Shared institution JWT used by multiple services |

All other service tokens (`PORTAL_MEDICAL_TOKEN`, `PORTAL_DMV_TOKEN`, etc.) are reused from the portal environment block.

---

## Anomaly Detection

The Anomalies page surfaces alerts from `sanly-bridge`'s built-in anomaly detection engine:

| Rule | Threshold | Severity |
|---|---|---|
| High-volume queries | >20 queries/hour per institution | MEDIUM |
| Repeated citizen query | Same citizen >3×/7 days | HIGH |
| Off-hours sensitive access | CRIMINAL_RECORD or MEDICAL_CLEARANCE accessed 22:00–06:00 | HIGH |
| Self-query suspicion | Sensitive data + ROUTINE_CHECK + no case reference | MEDIUM |
| Bulk citizen scan | >50 distinct citizens/hour | CRITICAL |
| Denied repeated attempt | >5 DENIED responses/hour | MEDIUM |

Alerts deduplicate within 24 h (no duplicate OPEN alerts for the same institution + type). Admins review alerts via PATCH /api/v1/anomalies/{id}/review; reviewed alerts show reviewer name and timestamp.

---

## Security Notes

- Admin JWT is stored in Zustand with `persist` (localStorage). Acceptable for prototype; production should use httpOnly cookies.
- Service tokens (`VITE_*_TOKEN`) are build-time injected and visible in browser network tab — this is a prototype simplification. Production deployments should use a Backend-for-Frontend server that holds service tokens server-side.
- NIN is masked in the audit log table (first 3 + last 2 digits). Full NIN is shown only in the detail modal.
- CSV export includes full NINs — treat exported files as sensitive government data.
