CREATE TABLE civil_officers (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(255) NOT NULL,
    national_id VARCHAR(11),
    office_region VARCHAR(100),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE officer_roles (
    officer_id BIGINT      NOT NULL REFERENCES civil_officers(id) ON DELETE CASCADE,
    role       VARCHAR(50) NOT NULL,
    PRIMARY KEY (officer_id, role)
);
