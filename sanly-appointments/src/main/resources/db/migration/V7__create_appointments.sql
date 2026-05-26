CREATE TABLE appointments (
    appointment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_code VARCHAR(22) UNIQUE NOT NULL,
    citizen_national_id VARCHAR(11) NOT NULL,
    office_code VARCHAR(12) NOT NULL,
    service_type_id UUID,
    appointment_date DATE NOT NULL,
    slot_time TIME NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'BOOKED',
    booked_at TIMESTAMP NOT NULL DEFAULT NOW(),
    notes TEXT,
    reminder_sent BOOLEAN NOT NULL DEFAULT FALSE,
    cancelled_at TIMESTAMP,
    cancel_reason VARCHAR(500),
    completed_by_officer_id BIGINT,
    completed_at TIMESTAMP,
    citizen_rating INT,
    citizen_feedback TEXT
);

CREATE INDEX idx_apt_citizen ON appointments (citizen_national_id);
CREATE INDEX idx_apt_office ON appointments (office_code);
CREATE INDEX idx_apt_date ON appointments (appointment_date);
CREATE INDEX idx_apt_status ON appointments (status);
CREATE INDEX idx_apt_office_date ON appointments (office_code, appointment_date);
