CREATE TABLE property_valuations (
    valuation_id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    cadastral_number      VARCHAR(30) NOT NULL,
    valuation_amount      TEXT        NOT NULL,
    valuation_date        DATE        NOT NULL,
    valued_by_officer_id  UUID,
    purpose               VARCHAR(20) NOT NULL,
    created_at            TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_valuations_cadastral ON property_valuations(cadastral_number);
