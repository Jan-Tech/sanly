-- V3: Consent requests table

CREATE TABLE IF NOT EXISTS consent_requests (
    consent_id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    consent_code        VARCHAR(30) NOT NULL,
    bank_code           VARCHAR(20) NOT NULL,
    citizen_national_id VARCHAR(20) NOT NULL,
    requested_scopes    TEXT,
    purpose             TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at        TIMESTAMP,
    expires_at          TIMESTAMP,
    approved_at         TIMESTAMP,
    rejected_at         TIMESTAMP,
    otp_verified        BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_consent_requests PRIMARY KEY (consent_id),
    CONSTRAINT uq_consent_requests_code UNIQUE (consent_code)
);

CREATE INDEX IF NOT EXISTS idx_consent_requests_citizen_status ON consent_requests (citizen_national_id, status);
CREATE INDEX IF NOT EXISTS idx_consent_requests_bank_status    ON consent_requests (bank_code, status);
CREATE INDEX IF NOT EXISTS idx_consent_requests_status_time    ON consent_requests (status, requested_at);

COMMENT ON TABLE  consent_requests IS 'Consent requests from banks to access citizen data';
COMMENT ON COLUMN consent_requests.consent_code IS 'Format: TM-CNS-YYYYNNNNNN';
COMMENT ON COLUMN consent_requests.requested_scopes IS 'JSON array of ConsentScope enum values';
COMMENT ON COLUMN consent_requests.purpose IS 'AES-256-GCM encrypted purpose string';
COMMENT ON COLUMN consent_requests.status IS 'PENDING, APPROVED, REJECTED, EXPIRED';
COMMENT ON COLUMN consent_requests.expires_at IS 'PENDING requests expire after 10 minutes';
