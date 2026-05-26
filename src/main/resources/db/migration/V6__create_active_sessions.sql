-- V6: Active login sessions for session management and revocation.
CREATE TABLE active_sessions (
    session_id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    national_id         VARCHAR(11)  NOT NULL,
    device_fingerprint  VARCHAR(64),  -- SHA-256 of User-Agent + IP
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    last_seen_at        TIMESTAMP    NOT NULL DEFAULT now(),
    expires_at          TIMESTAMP    NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    ip_address          VARCHAR(45),
    user_agent          TEXT
);

CREATE INDEX idx_sessions_national_id ON active_sessions(national_id);
CREATE INDEX idx_sessions_status      ON active_sessions(status);
CREATE INDEX idx_sessions_expires_at  ON active_sessions(expires_at) WHERE status = 'ACTIVE';
