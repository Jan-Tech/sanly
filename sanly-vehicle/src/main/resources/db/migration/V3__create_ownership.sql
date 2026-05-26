CREATE TABLE vehicle_ownerships (
    ownership_id         BIGSERIAL PRIMARY KEY,
    plate_number         VARCHAR(12) NOT NULL,
    owner_national_id    VARCHAR(20),
    owner_business_number VARCHAR(20),
    ownership_start_date DATE        NOT NULL,
    ownership_end_date   DATE,
    acquired_via         VARCHAR(20) NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_owner CHECK (owner_national_id IS NOT NULL OR owner_business_number IS NOT NULL)
);

CREATE INDEX idx_ownership_plate   ON vehicle_ownerships(plate_number);
CREATE INDEX idx_ownership_nin     ON vehicle_ownerships(owner_national_id);
CREATE INDEX idx_ownership_status  ON vehicle_ownerships(status);
