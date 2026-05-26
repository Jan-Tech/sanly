CREATE TABLE transfer_applications (
    application_id        UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    cadastral_number      VARCHAR(30) NOT NULL,
    from_national_id      VARCHAR(20) NOT NULL,
    to_national_id        VARCHAR(20) NOT NULL,
    transfer_type         VARCHAR(20) NOT NULL,
    agreed_price          TEXT,
    application_date      DATE        NOT NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_by_officer_id UUID,
    processed_at          TIMESTAMP,
    rejection_reason      TEXT,
    notes                 TEXT,
    created_at            TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transfers_cadastral ON transfer_applications(cadastral_number);
CREATE INDEX idx_transfers_from      ON transfer_applications(from_national_id);
CREATE INDEX idx_transfers_to        ON transfer_applications(to_national_id);
CREATE INDEX idx_transfers_status    ON transfer_applications(status);
