CREATE TABLE signature_verification_logs (
    log_id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signature_code       VARCHAR(22) NOT NULL,
    verified_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    verifier_ip          VARCHAR(45),
    result               VARCHAR(15) NOT NULL,
    verifier_national_id VARCHAR(11),
    document_resubmitted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_vlog_sig_code ON signature_verification_logs (signature_code);
CREATE INDEX idx_vlog_verifier ON signature_verification_logs (verifier_national_id);
