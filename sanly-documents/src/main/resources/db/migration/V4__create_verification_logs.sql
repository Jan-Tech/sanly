CREATE TABLE certificate_verification_logs (
    log_id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    certificate_code    VARCHAR(22) NOT NULL,
    verified_at         TIMESTAMP   NOT NULL DEFAULT NOW(),
    verifier_ip         VARCHAR(45),
    verifier_national_id VARCHAR(11),
    result              VARCHAR(10) NOT NULL
);

CREATE INDEX idx_vlog_cert_code ON certificate_verification_logs (certificate_code);
