-- V4: Consent tokens table

CREATE TABLE IF NOT EXISTS consent_tokens (
    token_id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    consent_code        VARCHAR(30) NOT NULL,
    token_hash          VARCHAR(64) NOT NULL,
    citizen_national_id VARCHAR(20) NOT NULL,
    bank_code           VARCHAR(20) NOT NULL,
    granted_scopes      TEXT,
    issued_at           TIMESTAMP,
    expires_at          TIMESTAMP,
    used_at             TIMESTAMP,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_consent_tokens PRIMARY KEY (token_id),
    CONSTRAINT uq_consent_tokens_hash UNIQUE (token_hash)
);

CREATE INDEX IF NOT EXISTS idx_consent_tokens_consent_code ON consent_tokens (consent_code);
CREATE INDEX IF NOT EXISTS idx_consent_tokens_status_expiry ON consent_tokens (status, expires_at);

COMMENT ON TABLE  consent_tokens IS 'Single-use access tokens issued when citizen approves consent';
COMMENT ON COLUMN consent_tokens.token_hash IS 'SHA-256 hex digest of the plain token (never stored in plaintext)';
COMMENT ON COLUMN consent_tokens.status IS 'ACTIVE, USED, EXPIRED, REVOKED';
COMMENT ON COLUMN consent_tokens.used_at IS 'Set when bank calls /citizen/profile — token becomes single-use';
COMMENT ON COLUMN consent_tokens.expires_at IS 'Tokens expire after 1 hour from issuance';
