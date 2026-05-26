CREATE TABLE property_sequences (
    year       INT    PRIMARY KEY,
    next_value BIGINT NOT NULL DEFAULT 1
);

CREATE TABLE properties (
    property_id      UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    cadastral_number VARCHAR(30)    NOT NULL UNIQUE,
    property_type    VARCHAR(30)    NOT NULL,
    address          TEXT           NOT NULL,
    region           VARCHAR(100)   NOT NULL,
    area             NUMERIC(12, 2),
    description      TEXT,
    status           VARCHAR(20)    NOT NULL DEFAULT 'REGISTERED',
    registered_at    TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_properties_region ON properties(region);
CREATE INDEX idx_properties_type   ON properties(property_type);
CREATE INDEX idx_properties_status ON properties(status);
