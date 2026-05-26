CREATE TABLE IF NOT EXISTS prescription_dispensings (
    dispensing_id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    prescription_code          VARCHAR(20) NOT NULL,
    dispensed_by_pharmacy_code VARCHAR(20) NOT NULL,
    dispensed_at               TIMESTAMP   NOT NULL DEFAULT NOW(),
    quantity_dispensed         INTEGER     NOT NULL,
    pharmacist_national_id     VARCHAR(11),
    notes                      TEXT
);

CREATE INDEX IF NOT EXISTS idx_disp_rx_code  ON prescription_dispensings (prescription_code);
CREATE INDEX IF NOT EXISTS idx_disp_pharmacy ON prescription_dispensings (dispensed_by_pharmacy_code);
