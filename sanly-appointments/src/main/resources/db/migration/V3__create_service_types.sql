CREATE TABLE service_types (
    service_type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    office_code VARCHAR(12) NOT NULL,
    institution_type VARCHAR(25) NOT NULL,
    service_name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    duration_minutes INT NOT NULL DEFAULT 30,
    requires_documents VARCHAR(1000),
    status VARCHAR(15) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_svc_office ON service_types (office_code);
