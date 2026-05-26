-- SANLY Medical — V1: Clinics

CREATE TABLE IF NOT EXISTS clinics (
    clinic_id       BIGSERIAL       NOT NULL,
    name            VARCHAR(200)    NOT NULL,
    license_number  VARCHAR(100)    NOT NULL,
    region          VARCHAR(100),
    address         VARCHAR(500),
    phone           VARCHAR(30),
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_clinics          PRIMARY KEY (clinic_id),
    CONSTRAINT uq_clinic_license   UNIQUE (license_number)
);

CREATE INDEX idx_clinics_status ON clinics (status);
CREATE INDEX idx_clinics_region ON clinics (region);
