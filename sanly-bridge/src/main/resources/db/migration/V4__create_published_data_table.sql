-- SANLY Bridge — V4: Published data registry
-- Institutions publish data records here. The bridge stores only metadata
-- and a reference (record_ref) back to the source institution's system.
-- No raw citizen data (medical records, tax details, etc.) is stored here.

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- needed for gen_random_uuid()

CREATE TABLE IF NOT EXISTS published_data (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    publisher_code  VARCHAR(50)     NOT NULL,
    national_id     VARCHAR(30)     NOT NULL,
    data_type       VARCHAR(50)     NOT NULL,
    record_ref      VARCHAR(500),               -- ID/URL in publisher's own system
    summary         TEXT,                        -- non-sensitive JSON metadata
    published_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMP,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_published_data  PRIMARY KEY (id),
    CONSTRAINT fk_pub_institution FOREIGN KEY (publisher_code)
        REFERENCES institutions (institution_code)
);

CREATE INDEX idx_pub_national_id  ON published_data (national_id);
CREATE INDEX idx_pub_data_type    ON published_data (data_type);
CREATE INDEX idx_pub_publisher    ON published_data (publisher_code);
CREATE INDEX idx_pub_active       ON published_data (active);
CREATE INDEX idx_pub_national_type ON published_data (national_id, data_type) WHERE active = TRUE;
