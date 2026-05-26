-- SANLY DMV — V2: License applications

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS license_applications (
    application_id              UUID            NOT NULL    DEFAULT gen_random_uuid(),
    citizen_national_id         VARCHAR(30)     NOT NULL,
    requested_category          VARCHAR(10)     NOT NULL,
    applied_at                  TIMESTAMP       NOT NULL    DEFAULT NOW(),
    status                      VARCHAR(15)     NOT NULL    DEFAULT 'PENDING'
                                    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    rejection_reason            TEXT,
    processed_by_officer_id     BIGINT,
    processed_at                TIMESTAMP,

    CONSTRAINT pk_license_applications  PRIMARY KEY (application_id),
    CONSTRAINT fk_app_officer           FOREIGN KEY (processed_by_officer_id)
        REFERENCES dmv_officers (officer_id)
);

CREATE INDEX idx_app_citizen    ON license_applications (citizen_national_id);
CREATE INDEX idx_app_status     ON license_applications (status);
CREATE INDEX idx_app_applied_at ON license_applications (applied_at DESC);
