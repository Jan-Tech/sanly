CREATE TABLE transfer_applications (
    application_id         BIGSERIAL PRIMARY KEY,
    plate_number           VARCHAR(12)  NOT NULL,
    from_national_id       VARCHAR(20)  NOT NULL,
    to_national_id         VARCHAR(20),
    to_business_number     VARCHAR(20),
    transfer_type          VARCHAR(20)  NOT NULL,
    agreed_price           VARCHAR(512),
    application_date       DATE         NOT NULL DEFAULT CURRENT_DATE,
    status                 VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    processed_by_officer_id BIGINT,
    processed_at           TIMESTAMP,
    rejection_reason       VARCHAR(500),
    CONSTRAINT chk_transfer_to CHECK (to_national_id IS NOT NULL OR to_business_number IS NOT NULL)
);

CREATE INDEX idx_transfer_plate  ON transfer_applications(plate_number);
CREATE INDEX idx_transfer_status ON transfer_applications(status);
CREATE INDEX idx_transfer_from   ON transfer_applications(from_national_id);
