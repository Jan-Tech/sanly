CREATE TABLE availability_schedules (
    schedule_id BIGSERIAL PRIMARY KEY,
    office_code VARCHAR(12) NOT NULL,
    day_of_week VARCHAR(3) NOT NULL,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    slot_duration_minutes INT NOT NULL DEFAULT 30,
    max_concurrent_appointments INT NOT NULL DEFAULT 3,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_schedule UNIQUE (office_code, day_of_week)
);
