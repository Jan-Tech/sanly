CREATE TABLE IF NOT EXISTS analytics_officers (
    officer_id      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(100) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50) NOT NULL DEFAULT 'ROLE_ADMIN',
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_officer_username UNIQUE (username)
);
