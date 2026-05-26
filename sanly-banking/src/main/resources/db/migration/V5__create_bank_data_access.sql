-- V5: Bank data access audit log

CREATE TABLE IF NOT EXISTS bank_data_accesses (
    access_id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    bank_code           VARCHAR(20) NOT NULL,
    citizen_national_id VARCHAR(20) NOT NULL,
    consent_code        VARCHAR(30) NOT NULL,
    scopes_accessed     TEXT,
    accessed_at         TIMESTAMP,
    ip_address          VARCHAR(45),
    response_status     VARCHAR(20),

    CONSTRAINT pk_bank_data_accesses PRIMARY KEY (access_id)
);

CREATE INDEX IF NOT EXISTS idx_bank_data_accesses_citizen  ON bank_data_accesses (citizen_national_id, accessed_at DESC);
CREATE INDEX IF NOT EXISTS idx_bank_data_accesses_bank     ON bank_data_accesses (bank_code, accessed_at DESC);
CREATE INDEX IF NOT EXISTS idx_bank_data_accesses_accessed ON bank_data_accesses (accessed_at DESC);

COMMENT ON TABLE  bank_data_accesses IS 'Immutable audit log of every data access by banks';
COMMENT ON COLUMN bank_data_accesses.response_status IS 'SUCCESS, PARTIAL, FAILED';
COMMENT ON COLUMN bank_data_accesses.scopes_accessed IS 'JSON array of ConsentScope enum values that were fetched';
COMMENT ON COLUMN bank_data_accesses.ip_address IS 'Bank server IP address (IPv4 or IPv6)';
