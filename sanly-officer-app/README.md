# SANLY Officer App

A React Native Expo mobile application for SANLY government field officers. Police officers, customs agents, medical staff, civil registry clerks, and other field personnel use this app to perform real-time citizen checks, scan SANLY codes, process applications, and register civil events — all in the field.

---

## Quick Start

### Prerequisites

- Node.js 18+
- Expo CLI: `npm install -g expo-cli`
- iOS Simulator (macOS) or Android Emulator, or the **Expo Go** app on a physical device

### Setup

```bash
# 1. Install dependencies
cd sanly-officer-app
npm install

# 2. Copy and configure environment variables
cp .env.example .env
# Edit .env with your gateway URL and service tokens

# 3. Start the Expo development server
npx expo start

# 4. Run on a platform
npx expo run:android   # Android emulator/device
npx expo run:ios       # iOS simulator/device (macOS only)
```

---

## Environment Configuration (`.env`)

| Variable | Description |
|---|---|
| `EXPO_PUBLIC_GATEWAY_URL` | SANLY API Gateway URL (default: `http://localhost:8080`) |
| `EXPO_PUBLIC_POLICE_TOKEN` | Bearer token for sanly-police service |
| `EXPO_PUBLIC_DMV_TOKEN` | Bearer token for sanly-dmv service |
| `EXPO_PUBLIC_MEDICAL_TOKEN` | Bearer token for sanly-medical service |
| `EXPO_PUBLIC_CUSTOMS_TOKEN` | Bearer token for sanly-customs service |
| `EXPO_PUBLIC_CIVIL_TOKEN` | Bearer token for sanly-civil service |
| `EXPO_PUBLIC_COURT_TOKEN` | Bearer token for sanly-court service |
| `EXPO_PUBLIC_EDUCATION_TOKEN` | Bearer token for sanly-education service |
| `EXPO_PUBLIC_LAND_TOKEN` | Bearer token for sanly-land service |
| `EXPO_PUBLIC_TAX_TOKEN` | Bearer token for sanly-tax service |
| `EXPO_PUBLIC_SOCIAL_TOKEN` | Bearer token for sanly-social service |
| `EXPO_PUBLIC_BRIDGE_INSTITUTION_CODE` | SANLY Bridge institution code |
| `EXPO_PUBLIC_BRIDGE_INSTITUTION_KEY` | SANLY Bridge institution key |

---

## Role Guide

Officers log in with credentials from the **citizen-registry** service (accounts with `ROLE_INSTITUTION`). The JWT role determines which features are visible in the **Actions** tab.

| Role | Institution | Actions Tab Shows |
|---|---|---|
| `ROLE_POLICE` | Police | Citizen check, criminal record submission, check history |
| `ROLE_DMV` | DMV | License lookup, pending applications queue (approve/reject) |
| `ROLE_MEDICAL` | Medical | Submit test results, today's record log |
| `ROLE_CUSTOMS` | Customs | Declaration lookup, pending queue (clear/hold) |
| `ROLE_CIVIL` | Civil Registry | Register birth, marriage, death (with offline queue) |
| `ROLE_COURT` | Court | Fine and case lookup, payment confirmation |
| `ROLE_EDUCATION` | Education | Diploma verification, pending school intake, issue diploma |
| `ROLE_LAND` | Land Registry | Property lookup, pending transfer approvals |
| `ROLE_TAX` | Tax Authority | Taxpayer lookup, filing queue (accept/reject) |
| `ROLE_SOCIAL` | Social Services | Benefit claims queue, register unemployment |
| `ROLE_ADMIN` | Admin | Appointments queue (all roles can see today's appointments) |

All roles can access the **Appointments** actions via the Scanner (scan `TM-APT-` codes) or via the dashboard quick action.

---

## QR Code Format Reference

The **Scan** tab can read all SANLY QR codes and barcodes. The system auto-detects the code type from its prefix:

| Code Format | Type | Example | Resolves To |
|---|---|---|---|
| `TM-NIN-XXXXXXXXXX` | National ID | `TM-NIN-1234567890` | Citizen profile |
| `TM-DL-YYYYNNNNNN` | Driving License | `TM-DL-2024000123` | License validity, holder, categories |
| `TM-BUS-YYYYNNNNNN` | Business Registration | `TM-BUS-2023000456` | Business info |
| `TM-APT-XXXXXXXX` | Appointment | `TM-APT-20240115` | Appointment details, citizen, service |
| `TM-FINE-XXXXXXXX` | Court Fine | `TM-FINE-ASH00123` | Fine amount, status, due date |
| `TM-CASE-XXXXXXXX` | Court Case | `TM-CASE-20240001` | Case type, status, next hearing |
| `TM-CAD-YYYYNNNNNN` | Cadastral (Land) | `TM-CAD-2021000789` | Property address, owner, status |
| `TM-DIP-YYYYNNNNNN` | Diploma | `TM-DIP-2022000234` | Degree, institution, validity |
| `TM-BIRTH-YYYYNNNNNN` | Birth Certificate | `TM-BIRTH-2024000011` | Birth record |
| `TM-MARR-YYYYNNNNNN` | Marriage Certificate | `TM-MARR-2024000022` | Marriage record |
| `TM-DEATH-YYYYNNNNNN` | Death Certificate | `TM-DEATH-2024000033` | Death record |
| `TM-PLATE-XX-XXX-XXX` | Vehicle Plate | `TM-PLATE-01-AB-234` | Vehicle info (via DMV lookup) |

Plain 10-digit numbers are treated as National IDs.

---

## Offline Mode

The app handles connectivity loss gracefully:

**What works offline:**
- Viewing scan history (last 20 scans cached in AsyncStorage)
- Browsing previously loaded data

**Offline queue:**
Civil registrations (birth/marriage/death) and medical test results are the most critical field operations. When submitted without connectivity, these are stored in an **offline queue** (AsyncStorage) and automatically synced when connection is restored.

The Profile tab shows a warning badge when offline items are pending. The Dashboard shows the queue count.

**Connectivity detection:** The app uses `@react-native-community/netinfo` to detect connectivity changes and automatically flushes the queue when connectivity is restored.

---

## Biometric Setup

1. **First login**: Enter Officer ID and password manually.
2. After successful login, go to **Profile → Settings → Biometrics** and enable "Login with Biometrics".
3. On subsequent app opens, the login screen will show a biometric button (Face ID / Fingerprint).
4. If biometric authentication fails 3 times, it falls back to password entry automatically.

Biometric state is stored in `expo-secure-store`. The JWT token itself is stored encrypted in secure storage and accessed only after successful biometric verification.

---

## Architecture Notes

```
App.tsx
└── QueryClientProvider (TanStack Query)
    └── NavigationContainer
        └── RootNavigator
            ├── AuthStack (when not logged in)
            │   └── LoginScreen
            └── MainTabs (when logged in)
                ├── HomeTab → DashboardScreen
                ├── ScanTab → ScanStack (Scanner → History → Result)
                ├── SearchTab → SearchStack (Search → CitizenDetail)
                ├── ActionsTab → ActionsStack (role-specific home)
                └── ProfileTab → ProfileStack (Profile → Settings)
```

**State management:**
- `authStore` (Zustand) — JWT token, officer profile, persisted to SecureStore
- `langStore` (Zustand) — language preference, persisted to SecureStore  
- `offlineQueueStore` (Zustand) — offline action queue, persisted to AsyncStorage
- TanStack Query — all server state (API calls, caching, invalidation)

**API routing:**
All calls route through `EXPO_PUBLIC_GATEWAY_URL`. Officer JWT (from citizen-registry login) is used for citizen lookups and bridge queries. Service tokens (from `.env`) are used for downstream service operations.

---

## Supported Languages

| Code | Language | Script |
|---|---|---|
| `en` | English | Latin |
| `tk` | Türkmen | Latin |
| `ru` | Русский | Cyrillic |

Language preference is stored locally and survives app restarts. The login screen always shows the language selector regardless of auth state.
