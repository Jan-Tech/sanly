# sanly-gateway

API Gateway for the SANLY e-governance platform. Single entry point (port **8080**) for all ten backend services.

## Responsibilities

| Concern | Implementation |
|---|---|
| Request tracing | `RequestIdFilter` — generates `X-Request-Id` UUID per request |
| Citizen context | `JwtValidationFilter` — soft-validates citizen-registry JWTs; adds `X-Citizen-NationalId` / `X-Citizen-Roles` headers |
| Rate limiting | `RateLimitFilter` — 200 req/min global, 20 req/min on `/auth/**` paths (per IP, Bucket4j) |
| Header security | `ServiceHeaderStripFilter` — strips internal headers from incoming requests to prevent spoofing |
| Routing | Spring Cloud Gateway declarative routes with `StripPrefix=1` |
| Aggregated docs | Swagger UI at `/swagger-ui.html` — dropdown per service |
| Health dashboard | `GET /health/services` — pings all 10 backends and returns aggregate status |

## Routing table

| External path | Backend service | Port |
|---|---|---|
| `/registry/**` | citizen-registry | 8090 |
| `/bridge/**` | sanly-bridge | 8091 |
| `/medical/**` | sanly-medical | 8082 |
| `/dmv/**` | sanly-dmv | 8083 |
| `/police/**` | sanly-police | 8084 |
| `/tax/**` | sanly-tax | 8085 |
| `/business/**` | sanly-business | 8086 |
| `/civil/**` | sanly-civil | 8087 |
| `/notifications/**` | sanly-notifications | 8088 |
| `/pharmacy/**` | sanly-pharmacy | 8089 |

`StripPrefix=1` removes the service prefix before forwarding, so `/medical/api/v1/records` → `http://sanly-medical:8082/api/v1/records`.

## Running locally

```bash
# From the root Practice/ directory:
make up          # starts all services including the gateway

# Gateway is the only public-facing port:
curl http://localhost:8080/health/services
curl http://localhost:8080/registry/actuator/health
curl http://localhost:8080/swagger-ui.html  # aggregated Swagger UI
```

## Required environment variables

| Variable | Description |
|---|---|
| `JWT_SECRET` | Same value as `REGISTRY_JWT_SECRET` — validates citizen JWTs |
| `REGISTRY_URL` | citizen-registry internal URL (docker: `http://citizen-registry:8090`) |
| `BRIDGE_URL` | sanly-bridge internal URL |
| `MEDICAL_URL` | sanly-medical internal URL |
| `DMV_URL` | sanly-dmv internal URL |
| `POLICE_URL` | sanly-police internal URL |
| `TAX_URL` | sanly-tax internal URL |
| `BUSINESS_URL` | sanly-business internal URL |
| `CIVIL_URL` | sanly-civil internal URL |
| `NOTIFICATIONS_URL` | sanly-notifications internal URL |
| `PHARMACY_URL` | sanly-pharmacy internal URL |

## Security notes

- Backend services are **not** exposed externally — only the gateway port (8080) is published in docker-compose.
- The `ServiceHeaderStripFilter` runs *before* `JwtValidationFilter`, so external callers cannot inject `X-Citizen-NationalId` or other internal headers.
- Rate limiting is per-IP and in-memory. For production, replace with Redis-backed Bucket4j.
