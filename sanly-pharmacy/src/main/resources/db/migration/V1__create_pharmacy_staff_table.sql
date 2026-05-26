CREATE TABLE IF NOT EXISTS pharmacy_staff (
    staff_id      BIGSERIAL    PRIMARY KEY,
    national_id   VARCHAR(11)  NOT NULL UNIQUE,
    first_name    VARCHAR(150) NOT NULL,
    last_name     VARCHAR(150) NOT NULL,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    pharmacy_code VARCHAR(20)  NOT NULL,
    role          VARCHAR(15)  NOT NULL DEFAULT 'PHARMACIST',
    status        VARCHAR(15)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_staff_username ON pharmacy_staff (username);
CREATE INDEX IF NOT EXISTS idx_staff_nin      ON pharmacy_staff (national_id);
