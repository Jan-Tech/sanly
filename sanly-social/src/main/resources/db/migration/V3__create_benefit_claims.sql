CREATE TABLE claim_sequences (
    year       INT    PRIMARY KEY,
    next_value BIGINT NOT NULL DEFAULT 1
);

CREATE TABLE benefit_claims (
    claim_id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_code            VARCHAR(30) NOT NULL UNIQUE,
    citizen_national_id   VARCHAR(20) NOT NULL,
    program_code          VARCHAR(20) NOT NULL,
    claim_type            VARCHAR(30) NOT NULL DEFAULT 'CITIZEN_APPLIED',
    status                VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_at            TIMESTAMP   NOT NULL DEFAULT NOW(),
    approved_at           TIMESTAMP,
    approved_by_officer_id UUID,
    expires_at            DATE,
    rejection_reason      TEXT,
    notes                 TEXT,
    trigger_reason        TEXT,
    created_at            TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_claims_citizen ON benefit_claims(citizen_national_id);
CREATE INDEX idx_claims_program ON benefit_claims(program_code);
CREATE INDEX idx_claims_status  ON benefit_claims(status);
