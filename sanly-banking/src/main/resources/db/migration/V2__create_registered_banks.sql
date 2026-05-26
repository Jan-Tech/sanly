-- V2: Registered banks table

CREATE TABLE IF NOT EXISTS registered_banks (
    bank_id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    bank_code             VARCHAR(20)  NOT NULL,
    bank_name             VARCHAR(200) NOT NULL,
    license_number        VARCHAR(100),
    contact_email         VARCHAR(200),
    api_key_hash          VARCHAR(255),
    status                VARCHAR(30)  NOT NULL DEFAULT 'PENDING_APPROVAL',
    registered_at         TIMESTAMP,
    approved_at           TIMESTAMP,
    approved_by_officer_id UUID,

    CONSTRAINT pk_registered_banks PRIMARY KEY (bank_id),
    CONSTRAINT uq_registered_banks_code UNIQUE (bank_code)
);

CREATE INDEX IF NOT EXISTS idx_registered_banks_status ON registered_banks (status);
CREATE INDEX IF NOT EXISTS idx_registered_banks_code   ON registered_banks (bank_code);

COMMENT ON TABLE  registered_banks IS 'Banks registered and approved to use the SANLY Banking API';
COMMENT ON COLUMN registered_banks.bank_code IS 'Format: TM-BNK-NNN';
COMMENT ON COLUMN registered_banks.api_key_hash IS 'BCrypt(12) hash of the raw API key';
COMMENT ON COLUMN registered_banks.status IS 'PENDING_APPROVAL, ACTIVE, SUSPENDED, REVOKED';
