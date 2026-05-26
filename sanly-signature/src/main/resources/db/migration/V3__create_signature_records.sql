CREATE TABLE signature_records (
    signature_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signature_code     VARCHAR(22) UNIQUE NOT NULL,
    signer_national_id VARCHAR(11) NOT NULL,
    document_hash      VARCHAR(64) NOT NULL,
    document_name      TEXT,
    document_size_bytes BIGINT,
    signature_value    VARCHAR(128) NOT NULL,
    purpose            TEXT,
    signed_at          TIMESTAMP NOT NULL,
    status             VARCHAR(10) NOT NULL DEFAULT 'VALID',
    revoked_at         TIMESTAMP,
    revoked_reason     TEXT,
    ip_address         VARCHAR(45),
    created_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sig_national_id ON signature_records (signer_national_id);
CREATE INDEX idx_sig_status      ON signature_records (status);
CREATE INDEX idx_sig_signed_at   ON signature_records (signed_at);
