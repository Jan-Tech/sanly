-- SANLY DMV — V4: License issuance audit log
-- Records every attempt to issue a license — success or failure.
-- Saved in a REQUIRES_NEW transaction so failures are captured even if the
-- outer transaction rolls back.

CREATE TABLE IF NOT EXISTS issuance_audit_log (
    id                      BIGSERIAL       NOT NULL,
    application_id          UUID,
    citizen_national_id     VARCHAR(30)     NOT NULL,
    category                VARCHAR(10)     NOT NULL,
    processed_by_officer_id BIGINT          NOT NULL,
    outcome                 VARCHAR(30)     NOT NULL,
    outcome_detail          TEXT,
    vision_test_ref         VARCHAR(500),
    license_number          VARCHAR(20),    -- populated on SUCCESS
    processed_at            TIMESTAMP       NOT NULL    DEFAULT NOW(),

    CONSTRAINT pk_issuance_audit_log PRIMARY KEY (id)
);

CREATE INDEX idx_ial_citizen    ON issuance_audit_log (citizen_national_id);
CREATE INDEX idx_ial_outcome    ON issuance_audit_log (outcome);
CREATE INDEX idx_ial_processed  ON issuance_audit_log (processed_at DESC);
