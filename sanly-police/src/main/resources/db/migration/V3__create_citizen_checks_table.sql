CREATE TABLE IF NOT EXISTS citizen_checks (
    check_id                UUID            NOT NULL    DEFAULT gen_random_uuid(),
    citizen_national_id     VARCHAR(30)     NOT NULL,
    checked_by_officer_id   BIGINT          NOT NULL,
    check_type              VARCHAR(20)     NOT NULL
                                CHECK (check_type IN ('DRIVING_LICENSE','TAX_STATUS','FULL_CHECK')),
    bridge_results          TEXT,                       -- AES-256-GCM encrypted JSON
    performed_at            TIMESTAMP       NOT NULL    DEFAULT NOW(),
    CONSTRAINT pk_citizen_checks PRIMARY KEY (check_id),
    CONSTRAINT fk_cc_officer FOREIGN KEY (checked_by_officer_id)
        REFERENCES police_officers (officer_id)
);

CREATE INDEX idx_cc_citizen     ON citizen_checks (citizen_national_id);
CREATE INDEX idx_cc_performed   ON citizen_checks (performed_at DESC);
