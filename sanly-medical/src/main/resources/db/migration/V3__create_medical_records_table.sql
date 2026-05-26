-- SANLY Medical — V3: Medical records
-- UUID primary key — serves as the bridge recordRef for external systems.
-- notes column is encrypted (AES-256-GCM) — raw values never stored.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS medical_records (
    record_id               UUID            NOT NULL DEFAULT gen_random_uuid(),
    citizen_national_id     VARCHAR(30)     NOT NULL,   -- reference to citizen-registry
    doctor_id               BIGINT          NOT NULL,
    clinic_id               BIGINT          NOT NULL,
    test_type               VARCHAR(30)     NOT NULL,
    result                  VARCHAR(10)     NOT NULL DEFAULT 'PENDING'
                                CHECK (result IN ('PASS', 'FAIL', 'PENDING')),
    notes                   TEXT,                       -- AES-256-GCM encrypted
    tested_at               TIMESTAMP       NOT NULL,
    expires_at              TIMESTAMP,
    bridge_published        BOOLEAN         NOT NULL DEFAULT FALSE,
    bridge_published_at     TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_medical_records   PRIMARY KEY (record_id),
    CONSTRAINT fk_mr_doctor         FOREIGN KEY (doctor_id)
        REFERENCES doctors (doctor_id),
    CONSTRAINT fk_mr_clinic         FOREIGN KEY (clinic_id)
        REFERENCES clinics (clinic_id)
);

CREATE INDEX idx_mr_citizen         ON medical_records (citizen_national_id);
CREATE INDEX idx_mr_clinic_id       ON medical_records (clinic_id);
CREATE INDEX idx_mr_doctor_id       ON medical_records (doctor_id);
CREATE INDEX idx_mr_test_type       ON medical_records (test_type);
CREATE INDEX idx_mr_result          ON medical_records (result);
CREATE INDEX idx_mr_bridge_pub      ON medical_records (bridge_published) WHERE bridge_published = FALSE;
CREATE INDEX idx_mr_citizen_clinic  ON medical_records (citizen_national_id, clinic_id);
