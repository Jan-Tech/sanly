-- SANLY Bridge — V5: Admin users (JWT auth for management endpoints)

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL       NOT NULL,
    username    VARCHAR(100)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_bridge_users    PRIMARY KEY (id),
    CONSTRAINT uq_bridge_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT      NOT NULL,
    role    VARCHAR(30) NOT NULL,

    CONSTRAINT pk_bridge_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_bridge_user_roles FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);
