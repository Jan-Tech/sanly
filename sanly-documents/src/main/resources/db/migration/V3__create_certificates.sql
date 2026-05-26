CREATE TABLE document_certificates (
    certificate_id     UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    certificate_code   VARCHAR(22)  UNIQUE NOT NULL,
    citizen_national_id VARCHAR(11) NOT NULL,
    holder_name        VARCHAR(200) NOT NULL,
    document_type      VARCHAR(30)  NOT NULL,
    source_service     VARCHAR(20)  NOT NULL,
    source_record_code VARCHAR(50),
    title              VARCHAR(300),
    issued_at          TIMESTAMP    NOT NULL,
    expires_at         TIMESTAMP,
    status             VARCHAR(10)  NOT NULL DEFAULT 'VALID',
    verification_hash  VARCHAR(64)  NOT NULL,
    download_count     INT          NOT NULL DEFAULT 0,
    last_downloaded_at TIMESTAMP,
    revoked_at         TIMESTAMP,
    revoke_reason      VARCHAR(500),
    pdf_content        TEXT
);

CREATE INDEX idx_cert_national_id ON document_certificates (citizen_national_id);
CREATE INDEX idx_cert_status      ON document_certificates (status);
CREATE INDEX idx_cert_type        ON document_certificates (document_type);
