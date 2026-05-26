CREATE TABLE unemployment_records (
    record_id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    citizen_national_id   VARCHAR(20) NOT NULL UNIQUE,
    registered_at         TIMESTAMP   NOT NULL DEFAULT NOW(),
    last_employer         TEXT,
    last_employment_date  DATE,
    reason                VARCHAR(30) NOT NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    updated_at            TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_unemployment_status ON unemployment_records(status);
CREATE INDEX idx_unemployment_reg    ON unemployment_records(registered_at);
