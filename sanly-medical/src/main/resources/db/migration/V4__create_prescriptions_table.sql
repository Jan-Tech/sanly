CREATE TABLE IF NOT EXISTS prescription_sequences (
    seq_year   INTEGER PRIMARY KEY,
    next_value BIGINT  NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS prescriptions (
    prescription_id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    prescription_code       VARCHAR(20)  NOT NULL UNIQUE,
    citizen_national_id     VARCHAR(11)  NOT NULL,
    issued_by_doctor_id     BIGINT       NOT NULL,
    clinic_id               BIGINT       NOT NULL,
    diagnosis_code          TEXT         NOT NULL,
    medication_name         VARCHAR(200) NOT NULL,
    medication_dosage       VARCHAR(100) NOT NULL,
    medication_form         VARCHAR(20)  NOT NULL,
    quantity                INTEGER      NOT NULL,
    unit                    VARCHAR(10)  NOT NULL,
    refills_allowed         INTEGER      NOT NULL DEFAULT 0,
    refills_used            INTEGER      NOT NULL DEFAULT 0,
    instructions            TEXT,
    issued_at               TIMESTAMP    NOT NULL DEFAULT NOW(),
    expires_at              DATE         NOT NULL,
    status                  VARCHAR(25)  NOT NULL DEFAULT 'ACTIVE',
    bridge_published        BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_rx_refills CHECK (refills_allowed BETWEEN 0 AND 5),
    CONSTRAINT chk_rx_refills_used CHECK (refills_used <= refills_allowed)
);

CREATE INDEX IF NOT EXISTS idx_rx_code   ON prescriptions (prescription_code);
CREATE INDEX IF NOT EXISTS idx_rx_nin    ON prescriptions (citizen_national_id);
CREATE INDEX IF NOT EXISTS idx_rx_doctor ON prescriptions (issued_by_doctor_id);
CREATE INDEX IF NOT EXISTS idx_rx_status ON prescriptions (status);
