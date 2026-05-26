CREATE TABLE insurance_records (
    insurance_id           BIGSERIAL PRIMARY KEY,
    plate_number           VARCHAR(12)  NOT NULL,
    insurance_company      VARCHAR(200) NOT NULL,
    policy_number          VARCHAR(512) NOT NULL,
    coverage_type          VARCHAR(30)  NOT NULL,
    valid_from             DATE         NOT NULL,
    valid_until            DATE         NOT NULL,
    status                 VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    registered_by_officer_id BIGINT
);

CREATE INDEX idx_insurance_plate  ON insurance_records(plate_number);
CREATE INDEX idx_insurance_status ON insurance_records(status);
CREATE INDEX idx_insurance_expiry ON insurance_records(valid_until) WHERE status = 'ACTIVE';
