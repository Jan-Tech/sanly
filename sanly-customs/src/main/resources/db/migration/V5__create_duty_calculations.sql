CREATE TABLE duty_calculations (
    calculation_id            UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    declaration_code          VARCHAR(30)    NOT NULL UNIQUE,
    hs_code                   VARCHAR(20),
    duty_rate_percent         NUMERIC(6,4)   NOT NULL,
    vat_rate_percent          NUMERIC(6,4)   NOT NULL DEFAULT 15.0,
    calculated_duty_amount    TEXT           NOT NULL,
    vat_amount                TEXT           NOT NULL,
    total_owed                TEXT           NOT NULL,
    duty_rate_override        TEXT,
    calculated_by_officer_id  UUID,
    calculated_at             TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_duty_declaration ON duty_calculations(declaration_code);
