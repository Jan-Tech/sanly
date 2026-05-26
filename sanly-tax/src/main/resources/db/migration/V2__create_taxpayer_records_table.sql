CREATE TABLE IF NOT EXISTS taxpayer_records (
    record_id               UUID            NOT NULL DEFAULT gen_random_uuid(),
    citizen_national_id     VARCHAR(30)     NOT NULL UNIQUE,
    tax_id                  VARCHAR(20)     NOT NULL UNIQUE,
    registration_date       DATE            NOT NULL,
    taxpayer_type           VARCHAR(15)     NOT NULL CHECK (taxpayer_type IN ('INDIVIDUAL','BUSINESS')),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                                CHECK (status IN ('ACTIVE','SUSPENDED','DEREGISTERED')),
    annual_income_class     TEXT,                   -- AES-256-GCM encrypted (LOW/MEDIUM/HIGH)
    registered_by_officer_id BIGINT         NOT NULL,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_taxpayer_records PRIMARY KEY (record_id),
    CONSTRAINT fk_tr_officer FOREIGN KEY (registered_by_officer_id)
        REFERENCES tax_officers (officer_id)
);
CREATE INDEX idx_tr_citizen ON taxpayer_records (citizen_national_id);
CREATE INDEX idx_tr_tax_id  ON taxpayer_records (tax_id);
