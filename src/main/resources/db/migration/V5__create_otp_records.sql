-- V5: OTP records for 2FA login, phone verification, and other sensitive actions.
CREATE TABLE otp_records (
    otp_id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    national_id         VARCHAR(11)  NOT NULL,
    session_token_hash  VARCHAR(64)  NOT NULL UNIQUE, -- SHA-256 hex of sessionToken
    otp_hash            VARCHAR(255) NOT NULL,          -- BCrypt of the 6-digit OTP
    purpose             VARCHAR(30)  NOT NULL DEFAULT 'LOGIN',
    phone_last_four     VARCHAR(4),
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    expires_at          TIMESTAMP    NOT NULL,
    attempts            INTEGER      NOT NULL DEFAULT 0,
    resend_count        INTEGER      NOT NULL DEFAULT 0,
    used                BOOLEAN      NOT NULL DEFAULT FALSE,
    ip_address          VARCHAR(45),
    locked_until        TIMESTAMP
);

CREATE INDEX idx_otp_national_id         ON otp_records(national_id);
CREATE INDEX idx_otp_session_token_hash  ON otp_records(session_token_hash);
CREATE INDEX idx_otp_created_at          ON otp_records(created_at);
