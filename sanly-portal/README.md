# SANLY Citizen Portal

React + TypeScript frontend for the SANLY e-governance platform. Citizens log in with their TM-NIN and see all their government data in one place.

## Stack
- React 18 + TypeScript
- Vite (build tool, dev proxy — no CORS issues)
- Tailwind CSS
- React Router v6
- Axios
- TanStack Query (React Query v5)
- Zustand (auth + language state)
- Lucide React (icons)

## Setup

```bash
npm install
cp .env.example .env
# Edit .env — fill in service tokens (see below)
npm run dev
# → http://localhost:5173
```

## Configuration (`.env`)

**Service URLs** — where each backend runs:
```
VITE_REGISTRY_URL=http://localhost:8080
VITE_MEDICAL_URL=http://localhost:8082
VITE_DMV_URL=http://localhost:8083
VITE_TAX_URL=http://localhost:8085
VITE_BUSINESS_URL=http://localhost:8086
VITE_CIVIL_URL=http://localhost:8087
VITE_BRIDGE_URL=http://localhost:8081
```

**Service tokens** — the portal uses pre-configured officer JWTs to read citizen data from each downstream service. To get them:
1. Start the service (e.g. `sanly-medical`)
2. POST to `/api/v1/auth/login` with the admin credentials
3. Copy the returned JWT into `.env`

```
VITE_MEDICAL_TOKEN=eyJhbGciOiJIUzI1NiJ9...
VITE_DMV_TOKEN=eyJhbGciOiJIUzI1NiJ9...
VITE_TAX_TOKEN=eyJhbGciOiJIUzI1NiJ9...
VITE_BUSINESS_TOKEN=eyJhbGciOiJIUzI1NiJ9...
VITE_CIVIL_TOKEN=eyJhbGciOiJIUzI1NiJ9...
```

**Bridge credentials** — for the Data Access Log:
```
VITE_BRIDGE_INSTITUTION_CODE=INST_PORTAL
VITE_BRIDGE_INSTITUTION_KEY=your_key_from_bridge
```
Register `INST_PORTAL` on SANLY Bridge first.

> **Security note**: In this demo, service tokens are client-side VITE_ variables. In production, use a Backend-for-Frontend (BFF) server to keep these credentials server-side.

## Dev Proxy

Vite proxies all API calls to avoid CORS issues:
- `/proxy/registry/*` → `VITE_REGISTRY_URL`
- `/proxy/medical/*` → `VITE_MEDICAL_URL`
- `/proxy/bridge/*` → `VITE_BRIDGE_URL`
- etc.

## Pages

| Route | Description |
|-------|-------------|
| `/login` | NIN + password login, language selector |
| `/dashboard` | Summary overview, recent data access |
| `/identity` | Full citizen record + civil certificates |
| `/medical` | Medical records with vision test highlight |
| `/license` | Driving license card + history |
| `/tax` | Tax registration + filing history |
| `/business` | Owned businesses |
| `/access-log` | Data access transparency log (Estonia model) |
| `/settings` | Language preference + change password |

## Languages
Turkmen (TK), Russian (RU), English (EN). Stored in localStorage via Zustand.

## Architecture

```
src/
├── api/         Axios clients per service
├── components/  Layout (Sidebar, Navbar), UI primitives
├── pages/       One file per page
├── store/       Zustand: authStore + langStore
├── types/       All TypeScript interfaces
└── utils/       i18n, NIN formatter, date, badge colors
```
