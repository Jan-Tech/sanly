CREATE TABLE IF NOT EXISTS registration_applications (
    application_id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    business_name           VARCHAR(300) NOT NULL,
    business_type           VARCHAR(25) NOT NULL,
    owner_national_id       VARCHAR(30) NOT NULL,
    requested_at            TIMESTAMP   NOT NULL DEFAULT NOW(),
    status                  VARCHAR(15) NOT NULL DEFAULT 'PENDING'
                                CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    rejection_reason        TEXT,
    processed_by_officer_id BIGINT,
    processed_at            TIMESTAMP,
    tax_check_result        TEXT,           -- JSON from bridge TAX_STATUS query
    criminal_check_result   TEXT,           -- JSON from bridge CRIMINAL_RECORD query
    tax_check_at            TIMESTAMP,
    criminal_check_at       TIMESTAMP,
    CONSTRAINT pk_reg_applications PRIMARY KEY (application_id),
    CONSTRAINT fk_app_officer FOREIGN KEY (processed_by_officer_id)
        REFERENCES registration_officers (officer_id)
);
CREATE INDEX idx_app_owner  ON registration_applications (owner_national_id);
CREATE INDEX idx_app_status ON registration_applications (status);
