# SANLY Appointments Service

Centralized government appointment booking system for all Turkmenistan institutions on the SANLY platform. Inspired by Azerbaijan's ASAN Service — citizens book online, arrive on time, skip the queue.

Port: **8101** | DB: **5450** | Gateway path: `/appointments/**`

---

## Booking Flow

```
Citizen opens SANLY Portal
        ↓
Selects institution type (DMV, Civil Registry, Tax, Court, etc.)
        ↓
Selects office (filtered by institution type + region)
        ↓
Selects service type (New License, Birth Registration, etc.)
        ↓
Picks a date (calendar showing green = available, gray = full/closed)
        ↓
Picks a time slot (visual grid — green available, gray full)
        ↓
Confirms booking with optional notes
        ↓
TM-APT-YYYYNNNNNN code issued + APPOINTMENT_BOOKED SMS sent
        ↓
Citizen arrives at office, shows QR code to officer
        ↓
Officer marks COMPLETED → APPOINTMENT_COMPLETED notification sent
        ↓
Citizen rates experience (1–5 stars) in portal
```

---

## Slot Calculation Algorithm

`SlotCalculationService.getAvailableSlots(officeCode, date, serviceTypeId)`:

1. Fetch `AvailabilitySchedule` for `officeCode` + day of week
2. Check `AvailabilityException` for the requested date
   - If exception exists and `isClosed = true` → return empty list
   - If exception exists but `isClosed = false` → use alternate open/close times
3. Generate all time slots from `openTime` to `closeTime` at `slotDurationMinutes` intervals
4. For each slot: `COUNT` existing BOOKED/CONFIRMED appointments at that exact slot
5. `spotsRemaining = maxConcurrentAppointments - count`
6. `available = spotsRemaining > 0`
7. Cache results for 60 seconds (cleared on booking/cancellation)
8. Return full list with available/spotsRemaining status

**Double-booking prevention**: The `book()` method uses `@Lock(PESSIMISTIC_WRITE)` on the slot count query, preventing concurrent bookings from both seeing the slot as available.

---

## Waitlist Logic

When a cancellation happens:
1. `triggerCheck(officeCode, serviceTypeId)` finds the first WAITING citizen
2. Sets status → NOTIFIED, sends `WAITLIST_SLOT_AVAILABLE` notification
3. Citizen has 2 hours to book
4. `WaitlistNotifierScheduler` (runs hourly) resets NOTIFIED entries older than 2 hours → WAITING, notifies next citizen
5. `WaitlistExpiryScheduler` (runs daily midnight) expires WAITING entries older than 30 days → sends `WAITLIST_EXPIRED`

---

## Appointment Code Format

`TM-APT-YYYYNNNNNN` — Year + 6-digit per-year sequence. Generated via PESSIMISTIC_WRITE locked sequence table.

Example: `TM-APT-2026000001`

---

## Seed Data (Ashgabat Offices)

| Code | Institution | Services |
|------|-------------|---------|
| TM-OFF-0001 | DMV | New License, Renewal, Category Change (30 min) |
| TM-OFF-0002 | Civil Registry | Birth, Marriage, Death, Name Change (45 min) |
| TM-OFF-0003 | Tax Authority | Consultation, Filing Assistance, Business Tax (30 min) |
| TM-OFF-0004 | Ashgabat District Court | Case Filing, Document Submission (30 min) |
| TM-OFF-0005 | Land Registry | Property Transfer, New Registration, Valuation (60 min) |
| TM-OFF-0006 | Social Services | Benefit Application, Pension Consultation (45 min) |

All offices: Mon–Fri 08:00–17:00, 30-min slots, max 3 concurrent per slot.

---

## API Summary

| Endpoint | Auth | Description |
|----------|------|-------------|
| `POST /auth/login` | None | Officer/admin login |
| `GET /offices` | None | List offices (filter by type/region) |
| `GET /slots/{officeCode}?date=&serviceTypeId=` | None | Available slots |
| `GET /slots/{officeCode}/next-available` | None | Next 5 dates with open slots |
| `POST /book` | Citizen JWT | Book appointment |
| `GET /my` | Citizen JWT | My appointments |
| `PATCH /my/{code}/cancel` | Citizen JWT | Cancel (≥2h before) |
| `POST /my/{code}/rate` | Citizen JWT | Rate completed appointment |
| `POST /waitlist` | Citizen JWT | Join waitlist |
| `GET /today` | Officer JWT | Today's schedule for my office |
| `PATCH /{code}/complete` | Officer JWT | Mark completed |
| `PATCH /{code}/no-show` | Officer JWT | Mark no-show |
| `GET /stats/{officeCode}` | Officer JWT | Office statistics |
| `GET /admin/all` | Admin JWT | All appointments |
| `GET /admin/stats` | Admin JWT | Platform-wide stats |

---

## Environment Variables

| Variable | Description |
|----------|-------------|
| `DB_URL` | PostgreSQL connection URL |
| `DB_USERNAME` | DB username |
| `DB_PASSWORD` | DB password |
| `JWT_SECRET` | Shared with citizen-registry (same secret, accepts citizen JWTs) |
| `ADMIN_USERNAME` | Seed admin username |
| `ADMIN_PASSWORD` | Seed admin password |
| `REGISTRY_URL` | Citizen registry base URL for verification |
| `REGISTRY_INSTITUTION_TOKEN` | JWT for citizen verification calls |
| `NOTIFICATION_SERVICE_URL` | Notification service URL |
| `NOTIFICATIONS_KEY_APPOINTMENTS` | Service key for notification calls |

---

## Schedulers

| Scheduler | Frequency | Action |
|-----------|-----------|--------|
| `ReminderScheduler` | Hourly | Sends APPOINTMENT_REMINDER for tomorrow's BOOKED appointments |
| `NoShowMarkingScheduler` | Every 30 min | Marks past-due BOOKED appointments as NO_SHOW |
| `WaitlistNotifierScheduler` | Hourly | Resets expired NOTIFIED waitlist entries, notifies next citizen |
| `WaitlistExpiryScheduler` | Daily midnight | Expires WAITING entries older than 30 days |
