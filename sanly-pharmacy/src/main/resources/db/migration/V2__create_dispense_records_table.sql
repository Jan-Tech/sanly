CREATE TABLE IF NOT EXISTS dispense_records (
    record_id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    prescription_code   VARCHAR(20) NOT NULL,
    citizen_national_id VARCHAR(11) NOT NULL,
    pharmacy_code       VARCHAR(20) NOT NULL,
    staff_id            BIGINT      NOT NULL,
    dispensed_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    quantity_dispensed  INTEGER     NOT NULL,
    notes               TEXT,
    bridge_verified     BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_dr_rx_code ON dispense_records (prescription_code);
CREATE INDEX IF NOT EXISTS idx_dr_citizen ON dispense_records (citizen_national_id);
CREATE INDEX IF NOT EXISTS idx_dr_staff   ON dispense_records (staff_id);
