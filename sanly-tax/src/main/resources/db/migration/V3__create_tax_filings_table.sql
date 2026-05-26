CREATE TABLE IF NOT EXISTS tax_filings (
    filing_id               UUID            NOT NULL DEFAULT gen_random_uuid(),
    tax_id                  VARCHAR(20)     NOT NULL,
    tax_year                INTEGER         NOT NULL,
    filing_status           VARCHAR(15)     NOT NULL DEFAULT 'SUBMITTED'
                                CHECK (filing_status IN ('PENDING','SUBMITTED','ACCEPTED','REJECTED')),
    declared_income         TEXT,                   -- encrypted
    tax_due                 TEXT,                   -- encrypted
    tax_paid                TEXT,                   -- encrypted
    submitted_at            TIMESTAMP       NOT NULL DEFAULT NOW(),
    processed_at            TIMESTAMP,
    processed_by_officer_id BIGINT,
    notes                   TEXT,
    CONSTRAINT pk_tax_filings       PRIMARY KEY (filing_id),
    CONSTRAINT uq_filing_year       UNIQUE (tax_id, tax_year),
    CONSTRAINT fk_filing_officer    FOREIGN KEY (processed_by_officer_id)
        REFERENCES tax_officers (officer_id)
);
CREATE INDEX idx_tf_tax_id  ON tax_filings (tax_id);
CREATE INDEX idx_tf_status  ON tax_filings (filing_status);
CREATE INDEX idx_tf_year    ON tax_filings (tax_year);
