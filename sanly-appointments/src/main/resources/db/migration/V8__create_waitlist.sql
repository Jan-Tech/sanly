CREATE TABLE waitlist_entries (
    waitlist_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    citizen_national_id VARCHAR(11) NOT NULL,
    office_code VARCHAR(12) NOT NULL,
    service_type_id UUID,
    preferred_date_from DATE,
    preferred_date_to DATE,
    status VARCHAR(15) NOT NULL DEFAULT 'WAITING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    notified_at TIMESTAMP
);

CREATE INDEX idx_wl_office_status ON waitlist_entries (office_code, status);
