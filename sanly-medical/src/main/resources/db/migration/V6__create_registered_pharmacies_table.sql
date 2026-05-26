CREATE TABLE IF NOT EXISTS registered_pharmacies (
    pharmacy_id     BIGSERIAL    PRIMARY KEY,
    pharmacy_code   VARCHAR(20)  NOT NULL UNIQUE,
    name            VARCHAR(200) NOT NULL,
    license_number  VARCHAR(100) NOT NULL UNIQUE,
    region          VARCHAR(100),
    address         TEXT,
    api_key_hash    VARCHAR(100) NOT NULL,
    status          VARCHAR(15)  NOT NULL DEFAULT 'ACTIVE',
    registered_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pharmacy_code ON registered_pharmacies (pharmacy_code);
