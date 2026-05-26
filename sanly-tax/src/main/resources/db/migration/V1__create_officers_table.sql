CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tax_officers (
    officer_id      BIGSERIAL   NOT NULL,
    national_id     CHAR(11)            UNIQUE,
    first_name      VARCHAR(150) NOT NULL,
    last_name       VARCHAR(150) NOT NULL,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    office_region   VARCHAR(100),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE','SUSPENDED')),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_tax_officers PRIMARY KEY (officer_id)
);
CREATE TABLE IF NOT EXISTS officer_roles (
    officer_id  BIGINT      NOT NULL,
    role        VARCHAR(30) NOT NULL,
    CONSTRAINT pk_officer_roles PRIMARY KEY (officer_id, role),
    CONSTRAINT fk_officer_roles FOREIGN KEY (officer_id)
        REFERENCES tax_officers (officer_id) ON DELETE CASCADE
);
