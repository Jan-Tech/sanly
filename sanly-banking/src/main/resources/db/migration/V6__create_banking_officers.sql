-- V6: Banking officers (admin users)

CREATE TABLE IF NOT EXISTS banking_officers (
    officer_id    UUID         NOT NULL DEFAULT gen_random_uuid(),
    username      VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL DEFAULT 'ROLE_OFFICER',
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP,

    CONSTRAINT pk_banking_officers PRIMARY KEY (officer_id),
    CONSTRAINT uq_banking_officers_username UNIQUE (username)
);

CREATE INDEX IF NOT EXISTS idx_banking_officers_username ON banking_officers (username, active);

COMMENT ON TABLE  banking_officers IS 'Admin officers who manage the SANLY Banking API';
COMMENT ON COLUMN banking_officers.password_hash IS 'BCrypt(12) hash of the officer password';
COMMENT ON COLUMN banking_officers.role IS 'ROLE_OFFICER or ROLE_ADMIN';
