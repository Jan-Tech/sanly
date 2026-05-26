-- SANLY Citizen Registry — V3: Users, roles, and NIN sequence table

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL       NOT NULL,
    username    VARCHAR(100)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    national_id CHAR(11),            -- linked citizen (for ROLE_CITIZEN accounts)
    enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT      NOT NULL,
    role    VARCHAR(30) NOT NULL,

    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

-- NIN sequence counters — one row per (gender+century code + YYMMDD) prefix.
-- Pessimistic-locked by the application to guarantee uniqueness.
CREATE TABLE IF NOT EXISTS nin_sequences (
    prefix          CHAR(7)     NOT NULL,  -- e.g. '5900315' (code=5, YY=90, MM=03, DD=15)
    last_sequence   INTEGER     NOT NULL DEFAULT 0,

    CONSTRAINT pk_nin_sequences PRIMARY KEY (prefix),
    CONSTRAINT chk_nin_seq_range CHECK (last_sequence BETWEEN 0 AND 999)
);
