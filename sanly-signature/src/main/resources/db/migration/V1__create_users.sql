CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(100) UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id),
    role    VARCHAR(30) NOT NULL
);
