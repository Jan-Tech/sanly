CREATE TABLE IF NOT EXISTS data_deletion_requests (
    request_id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    request_code            VARCHAR(22)  UNIQUE NOT NULL,
    citizen_national_id     VARCHAR(11)  NOT NULL,
    request_type            VARCHAR(30)  NOT NULL,
    affected_service        VARCHAR(25)  NOT NULL,
    description             TEXT,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    submitted_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    reviewed_at             TIMESTAMP,
    reviewed_by_officer_id  VARCHAR(100),
    reviewer_notes          TEXT,
    resolution_description  TEXT,
    export_data             TEXT
);

CREATE INDEX idx_ddr_national_id ON data_deletion_requests (citizen_national_id);
CREATE INDEX idx_ddr_status      ON data_deletion_requests (status);
CREATE INDEX idx_ddr_type        ON data_deletion_requests (request_type);
