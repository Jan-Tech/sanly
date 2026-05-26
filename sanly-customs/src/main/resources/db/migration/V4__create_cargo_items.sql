CREATE TABLE cargo_items (
    item_id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    declaration_code VARCHAR(30) NOT NULL,
    item_description TEXT,
    hs_code          VARCHAR(20),
    quantity         VARCHAR(50),
    unit             VARCHAR(20),
    unit_value       TEXT,
    total_value      TEXT,
    country_of_origin VARCHAR(100),
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cargo_declaration ON cargo_items(declaration_code);
