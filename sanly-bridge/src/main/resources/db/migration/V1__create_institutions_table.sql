-- SANLY Bridge — V1: Institutions
-- institutionCode is the primary key and is used as a foreign key in all other tables.
-- API keys are stored as BCrypt hashes — the raw key is never persisted.

CREATE TABLE IF NOT EXISTS institutions (
    institution_code    VARCHAR(50)     NOT NULL,
    name                VARCHAR(200)    NOT NULL,
    description         TEXT,
    hashed_api_key      VARCHAR(255)    NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING')),
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_institutions PRIMARY KEY (institution_code)
);

-- Data types this institution is authorized to publish
CREATE TABLE IF NOT EXISTS institution_publishable_types (
    institution_code    VARCHAR(50)     NOT NULL,
    data_type           VARCHAR(50)     NOT NULL,

    CONSTRAINT pk_inst_pub_types  PRIMARY KEY (institution_code, data_type),
    CONSTRAINT fk_inst_pub_types  FOREIGN KEY (institution_code)
        REFERENCES institutions (institution_code) ON DELETE CASCADE
);

CREATE INDEX idx_institutions_status ON institutions (status);
