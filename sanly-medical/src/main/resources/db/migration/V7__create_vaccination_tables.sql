CREATE TABLE IF NOT EXISTS vaccination_schedules (
    schedule_id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    citizen_national_id VARCHAR(11) NOT NULL UNIQUE,
    mother_national_id  VARCHAR(11),
    father_national_id  VARCHAR(11),
    schedule_status     VARCHAR(15) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_vs_citizen ON vaccination_schedules (citizen_national_id);

CREATE TABLE IF NOT EXISTS vaccination_appointments (
    appointment_id      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_id         UUID        NOT NULL REFERENCES vaccination_schedules(schedule_id),
    citizen_national_id VARCHAR(11) NOT NULL,
    vaccine_name        VARCHAR(100) NOT NULL,
    due_date            DATE        NOT NULL,
    status              VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    completed_at        TIMESTAMP,
    completed_by_doctor_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_va_schedule ON vaccination_appointments (schedule_id);
CREATE INDEX IF NOT EXISTS idx_va_due_date ON vaccination_appointments (due_date);
CREATE INDEX IF NOT EXISTS idx_va_status   ON vaccination_appointments (status);
