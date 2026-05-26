# sanly-signature

Digital Signature Service for the **SANLY e-governance platform** (Turkmenistan).

This microservice allows citizens to digitally sign documents, verify signature authenticity, and provides admins with oversight tools and statistics. Every signed document receives a unique TM-SIG code and a QR code for public verification.

---

## Purpose

Provides legally-attributable digital signatures over documents submitted through SANLY citizen services. Each signature is:
- Cryptographically bound to the citizen's national ID
- Tied to the exact document content via SHA-256 hash
- Protected by HMAC-SHA256 to prevent server-side forgery
- Assigned a globally unique TM-SIG code for easy reference

---

## How Signing Works (Step by Step)

1. **Citizen authenticates** with the citizen-registry service and obtains a JWT containing their `nationalId` claim.
2. **Citizen requests an action OTP** from the citizen-registry (out-of-band — SMS or app).
3. **Citizen calls `POST /api/v1/signature/sign`** with:
   - The document file (any format, up to 10 MB)
   - The intended purpose (e.g., "Loan application", "Property transfer")
   - The OTP code
4. **sanly-signature verifies the OTP** by calling `citizen-registry /api/v1/auth/verify-action-otp`.
5. **SHA-256 hash** of the document bytes is computed server-side.
6. **A unique TM-SIG code** is generated (e.g., `TM-SIG-202600000042`) using a locked sequence table to prevent duplicates.
7. **HMAC-SHA256** is computed over `documentHash|nationalId|signedAt` using a secret server key.
8. The `SignatureRecord` is persisted to PostgreSQL with the hash, HMAC value, code, and encrypted metadata.
9. Asynchronously: the event is published to `sanly-bridge` and the citizen receives a notification.
10. The response includes the `signatureCode` and all record details.

---

## Cryptography: HMAC-SHA256 vs RSA

**This implementation uses server-side HMAC-SHA256.**

- The server holds a shared secret (`SERVER_SIGNING_SECRET`).
- The signature is: `HMAC-SHA256(documentHash + "|" + nationalId + "|" + signedAt, secret)`.
- Anyone with the secret can verify (or forge) a signature — so **trust in the server is required**.

**Production upgrade path: RSA-2048 / ECDSA with citizen keys**

In a production system, each citizen would have a private key stored on their national ID card chip (similar to Estonia's ID-kaart). The signing flow would:
1. Send the document hash to the citizen's card reader.
2. The citizen's private key signs the hash on the card — the private key never leaves the card.
3. The server stores the resulting RSA/ECDSA signature and the citizen's public key certificate.
4. Verification requires only the public key — no server secret is involved.

This approach provides **non-repudiation**: the citizen cannot deny signing because only they hold the private key. The HMAC approach in this prototype is cryptographically secure but does not provide non-repudiation, since the server could theoretically create signatures without the citizen's involvement.

---

## Verification Flow

### Full Verification (Document Re-upload)

`POST /api/v1/signature/verify` — multipart with `file` + `signatureCode`

1. Looks up the `SignatureRecord` by code.
2. Computes SHA-256 of the uploaded file.
3. Compares with the stored `documentHash`.
4. Recomputes HMAC and compares with stored `signatureValue`.
5. Returns `valid: true` only if all checks pass and status is `VALID`.

### Metadata Verification (Code Only)

`GET /api/v1/signature/verify/{signatureCode}` — no file needed

Returns signature metadata (signer, date, purpose, status) without verifying document integrity. Useful for QR code scanning to confirm a signature exists and is not revoked.

---

## QR Code

Each signature can generate a QR code via `GET /api/v1/signature/{signatureCode}/qr`.

The QR encodes: `https://sanly.tm/verify/{signatureCode}`

**Embed the QR code in signed documents** (e.g., PDF footer). Anyone with a smartphone can scan it to reach the public verification page and confirm the document's signature status.

---

## Legal Standing

> Digital signatures under this system require enabling legislation (e.g., a Turkmenistan Electronic Signature Law analogous to the EU's eIDAS Regulation) to have the same legal standing as handwritten signatures. Until such legislation is enacted, these signatures serve as strong technical attestations but may not be legally equivalent to wet signatures in Turkmenistan courts.

---

## Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `DB_URL` | No | `jdbc:postgresql://localhost:5449/sanly_signature` | PostgreSQL JDBC URL |
| `DB_USERNAME` | No | `signature_user` | Database username |
| `DB_PASSWORD` | **Yes** | — | Database password |
| `JWT_SECRET` | **Yes** | — | JWT signing secret (min 32 chars) |
| `JWT_EXPIRATION_MS` | No | `86400000` | JWT lifetime in ms (24h) |
| `ENCRYPTION_KEY` | **Yes** | — | AES-256 key — exactly 32 chars |
| `SERVER_SIGNING_SECRET` | **Yes** | — | HMAC-SHA256 signing secret (min 32 chars) |
| `ADMIN_USERNAME` | No | `admin` | Admin account username |
| `ADMIN_PASSWORD` | **Yes** | — | Admin account password |
| `REGISTRY_URL` | No | `http://localhost:8080` | citizen-registry base URL |
| `SERVICE_KEY` | **Yes** | — | Service-to-service key for registry |
| `NOTIFICATION_SERVICE_URL` | No | `http://localhost:8088` | Notification service URL |
| `NOTIFICATIONS_KEY_SIGNATURE` | No | `signature-default-key-change-me` | Notification service key |
| `BRIDGE_URL` | No | `http://localhost:8081` | sanly-bridge base URL |
| `BRIDGE_INSTITUTION_KEY_SIGNATURE` | **Yes** | — | Bridge institution auth key |
| `QR_VERIFY_BASE_URL` | No | `https://sanly.tm/verify` | Base URL embedded in QR codes |

---

## API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/signature/auth/login` | None | Admin login — returns JWT |
| `POST` | `/api/v1/signature/sign` | CITIZEN JWT | Sign a document |
| `GET` | `/api/v1/signature/my` | CITIZEN JWT | List my signatures |
| `GET` | `/api/v1/signature/{code}` | CITIZEN or ADMIN | Get signature detail |
| `PATCH` | `/api/v1/signature/{code}/revoke` | CITIZEN or ADMIN | Revoke a signature |
| `GET` | `/api/v1/signature/{code}/qr` | CITIZEN or ADMIN | Get QR code PNG |
| `POST` | `/api/v1/signature/verify` | None (public) | Verify with document re-upload |
| `GET` | `/api/v1/signature/verify/{code}` | None (public) | Verify by code (metadata only) |
| `GET` | `/api/v1/signature/admin/all` | ADMIN | All signatures (paginated) |
| `GET` | `/api/v1/signature/admin/stats` | ADMIN | Platform statistics |
| `GET` | `/swagger-ui.html` | None | Swagger UI |
| `GET` | `/actuator/health` | None | Health check |

---

## Running Locally

```bash
# Start PostgreSQL
docker-compose up db-signature -d

# Set required environment variables
export DB_PASSWORD=signature_pass
export JWT_SECRET=your-32-char-jwt-secret-here!!!!
export ENCRYPTION_KEY=your-32-char-encryption-key-here
export SERVER_SIGNING_SECRET=your-32-char-signing-secret-here
export ADMIN_PASSWORD=secureadminpassword
export SERVICE_KEY=your-registry-service-key
export BRIDGE_INSTITUTION_KEY_SIGNATURE=your-bridge-key

# Run
mvn spring-boot:run
```

The service starts on **http://localhost:8100**.

Swagger UI: http://localhost:8100/swagger-ui.html
