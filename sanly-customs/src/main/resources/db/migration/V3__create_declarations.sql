CREATE TABLE declaration_sequences (
    year       INTEGER PRIMARY KEY,
    next_value INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE customs_declarations (
    declaration_id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    declaration_code          VARCHAR(30)  NOT NULL UNIQUE,
    declarant_type            VARCHAR(20)  NOT NULL,
    declarant_national_id     VARCHAR(20),
    declarant_business_number VARCHAR(50),
    declaration_type          VARCHAR(20)  NOT NULL,
    port_code                 VARCHAR(20)  NOT NULL,
    cargo_description         TEXT,
    hs_code                   VARCHAR(20),
    country_of_origin         VARCHAR(100),
    country_of_destination    VARCHAR(100),
    quantity                  VARCHAR(50),
    unit                      VARCHAR(20),
    declared_value            TEXT,
    currency                  VARCHAR(10)  DEFAULT 'TMT',
    duties_owed               TEXT,
    duties_paid               TEXT         DEFAULT '0',
    declaration_date          DATE         NOT NULL DEFAULT CURRENT_DATE,
    status                    VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    processed_by_officer_id   UUID,
    processed_at              TIMESTAMP,
    rejection_reason          TEXT,
    notes                     TEXT,
    created_at                TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_decl_declarant_nin    ON customs_declarations(declarant_national_id);
CREATE INDEX idx_decl_declarant_biz    ON customs_declarations(declarant_business_number);
CREATE INDEX idx_decl_port_status      ON customs_declarations(port_code, status);
CREATE INDEX idx_decl_status           ON customs_declarations(status);
CREATE INDEX idx_decl_date             ON customs_declarations(declaration_date);
