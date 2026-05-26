CREATE TABLE government_offices (
    office_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    office_code VARCHAR(12) UNIQUE NOT NULL,
    institution_type VARCHAR(25) NOT NULL,
    name VARCHAR(200) NOT NULL,
    region VARCHAR(100) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(30),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    status VARCHAR(25) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_offices_type ON government_offices (institution_type);
CREATE INDEX idx_offices_region ON government_offices (region);
