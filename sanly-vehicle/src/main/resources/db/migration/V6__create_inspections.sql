CREATE TABLE technical_inspections (
    inspection_id          BIGSERIAL PRIMARY KEY,
    plate_number           VARCHAR(12) NOT NULL,
    inspection_date        DATE        NOT NULL,
    next_inspection_due    DATE        NOT NULL,
    result                 VARCHAR(20) NOT NULL,
    findings               TEXT,
    inspected_by_officer_id BIGINT
);

CREATE INDEX idx_inspection_plate      ON technical_inspections(plate_number);
CREATE INDEX idx_inspection_next_due   ON technical_inspections(next_inspection_due);
