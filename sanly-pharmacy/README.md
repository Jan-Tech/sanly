# sanly-pharmacy — e-Prescription Dispensing Service

Enables pharmacy staff to verify and dispense digital prescriptions issued by doctors in sanly-medical. Citizens never need paper prescriptions — they present their TM-NIN or prescription code at the counter.

**Port:** 8089  
**Auth:** JWT (pharmacy staff login)  
**Bridge institution:** INST_PHARMACY (queries PRESCRIPTION from INST_MEDICAL)

---

## The e-Prescription Flow

```
1. Doctor (sanly-medical) issues TM-RX-2026000001
   → Published to SANLY Bridge as PRESCRIPTION

2. Citizen visits any registered pharmacy
   → Gives pharmacist their TM-NIN or prescription code

3. Pharmacist looks up prescription via sanly-pharmacy
   → Bridge returns: ACTIVE, 1 refill remaining, expires 2026-06-18

4. Pharmacist confirms identity and dispenses medication
   → POST /api/v1/dispense → calls sanly-medical → updates refillsUsed
   → DispenseRecord saved locally
   → Citizen notified: "City Pharmacy dispensed Amoxicillin 500mg"

5. If citizen tries same prescription again at another pharmacy:
   → Status: FULLY_DISPENSED
   → System rejects: "This prescription has been fully dispensed"
```

---

## API Reference

### Auth

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/auth/login` | Staff login — returns JWT |

### Prescription Lookup (ROLE_PHARMACIST or ROLE_MANAGER)

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/prescriptions/{code}?nationalId={nin}` | Look up by code + NIN |
| GET | `/api/v1/prescriptions/citizen/{nin}` | All active prescriptions for citizen |

### Dispensing (ROLE_PHARMACIST or ROLE_MANAGER)

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/dispense` | Dispense a prescription |
| GET | `/api/v1/dispense/history` | This pharmacy's dispensing history |
| GET | `/api/v1/dispense/history/citizen/{nin}` | Dispensing history for a citizen |

### Staff Management (ROLE_MANAGER)

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/staff` | Add a pharmacist or manager |
| GET | `/api/v1/staff` | List all staff |
| PATCH | `/api/v1/staff/{staffId}/status` | Activate/suspend staff |

---

## Bridge Setup

Register INST_PHARMACY in sanly-bridge and grant permission to query PRESCRIPTION from INST_MEDICAL:

```bash
# 1. Login to bridge as admin → get JWT
TOKEN=$(curl -s -X POST localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"..."}' | jq -r .token)

# 2. Register INST_PHARMACY
curl -X POST localhost:8081/api/v1/institutions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"institutionCode":"INST_PHARMACY","name":"Sanly Pharmacy Network",
       "publishableTypes":[]}'
# → copy the one-time API key into .env as BRIDGE_INSTITUTION_KEY_PHARMACY

# 3. Grant permission: INST_PHARMACY may query PRESCRIPTION from INST_MEDICAL
curl -X POST localhost:8081/api/v1/permissions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"requestingCode":"INST_PHARMACY","targetCode":"INST_MEDICAL","dataType":"PRESCRIPTION"}'
```

---

## Pharmacy Registration (in sanly-medical)

Each pharmacy running this service must be registered in sanly-medical to get an API key:

```bash
# Login to sanly-medical as admin → get JWT
MEDICAL_TOKEN=...

# Register pharmacy
curl -X POST localhost:8082/api/v1/pharmacies \
  -H "Authorization: Bearer $MEDICAL_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"City Pharmacy #1","licenseNumber":"TM-PHAR-001","region":"Ashgabat"}'
# → copy the one-time API key into .env as PHARMACY_API_KEY
# → copy the pharmacyCode (e.g. TM-PHR-0001) into .env as PHARMACY_CODE
```

---

## Configuration

Key env vars (see root `.env.example`):

| Variable | Description |
|---|---|
| `PHARMACY_CODE` | This pharmacy's code (e.g. `TM-PHR-0001`) from sanly-medical registration |
| `PHARMACY_API_KEY` | Raw API key issued by sanly-medical at registration |
| `BRIDGE_INSTITUTION_KEY` | INST_PHARMACY API key from sanly-bridge |
| `CITIZEN_REGISTRY_TOKEN` | ROLE_INSTITUTION JWT for citizen name lookups |
| `NOTIFICATION_SERVICE_KEY` | Key for sending citizen notifications |

---

## Security Notes

- JWT for staff endpoints; default session lifetime 24 h
- MANAGER role required for staff management; PHARMACIST role for dispensing
- Pharmacy identity (code + key) verified by sanly-medical on every dispense call via `PharmacyKeyAuthFilter`
- Double-dispensing is prevented by pessimistic DB lock in sanly-medical
- All dispensing attempts are logged locally in `dispense_records`
