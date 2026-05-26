-- SANLY DMV — V3: Driving licenses
-- license_number format: TM-DL-YYYYNNNNNN (e.g. TM-DL-2026000001)

CREATE TABLE IF NOT EXISTS driving_licenses (
    license_id              UUID            NOT NULL    DEFAULT gen_random_uuid(),
    citizen_national_id     VARCHAR(30)     NOT NULL,
    license_number          VARCHAR(20)     NOT NULL,
    category                VARCHAR(10)     NOT NULL,
    issued_at               TIMESTAMP       NOT NULL,
    expires_at              TIMESTAMP       NOT NULL,
    issued_by_officer_id    BIGINT          NOT NULL,
    status                  VARCHAR(15)     NOT NULL    DEFAULT 'ACTIVE'
                                CHECK (status IN ('ACTIVE', 'SUSPENDED', 'EXPIRED', 'REVOKED')),
    vision_test_ref         VARCHAR(500),   -- recordRef from SANLY Bridge
    notes                   TEXT,
    created_at              TIMESTAMP       NOT NULL    DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL    DEFAULT NOW(),

    CONSTRAINT pk_driving_licenses      PRIMARY KEY (license_id),
    CONSTRAINT uq_license_number        UNIQUE (license_number),
    CONSTRAINT fk_license_officer       FOREIGN KEY (issued_by_officer_id)
        REFERENCES dmv_officers (officer_id)
);

CREATE INDEX idx_lic_citizen        ON driving_licenses (citizen_national_id);
CREATE INDEX idx_lic_number         ON driving_licenses (license_number);
CREATE INDEX idx_lic_status         ON driving_licenses (status);
CREATE INDEX idx_lic_expires        ON driving_licenses (expires_at);
CREATE INDEX idx_lic_citizen_cat    ON driving_licenses (citizen_national_id, category, status);
