-- SANLY Citizen Registry — V1: Citizens table
-- National IDs (TM-NIN) are 11-digit strings, primary key.
-- Encrypted columns use TEXT to accommodate base64-encoded AES-GCM ciphertext.

CREATE TABLE IF NOT EXISTS citizens (
    national_id     CHAR(11)        NOT NULL,
    first_name      VARCHAR(150)    NOT NULL,
    last_name       VARCHAR(150)    NOT NULL,
    middle_name     VARCHAR(150),
    date_of_birth   DATE            NOT NULL,
    gender          VARCHAR(6)      NOT NULL CHECK (gender IN ('MALE', 'FEMALE')),
    place_of_birth  TEXT,                          -- encrypted
    street          TEXT,                          -- encrypted
    city            VARCHAR(150),
    region          VARCHAR(100),
    photo_url       TEXT,                          -- encrypted
    father_id       CHAR(11),
    mother_id       CHAR(11),
    spouse_id       CHAR(11),
    status          VARCHAR(10)     NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'DECEASED', 'SUSPENDED')),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_citizens PRIMARY KEY (national_id)
);

-- Search-optimised indexes
CREATE INDEX idx_citizens_last_name   ON citizens (last_name);
CREATE INDEX idx_citizens_first_name  ON citizens (first_name);
CREATE INDEX idx_citizens_dob         ON citizens (date_of_birth);
CREATE INDEX idx_citizens_region      ON citizens (region);
CREATE INDEX idx_citizens_status      ON citizens (status);
