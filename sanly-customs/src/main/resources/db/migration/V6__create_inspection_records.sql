CREATE TABLE inspection_records (
    inspection_id       UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    declaration_code    VARCHAR(30) NOT NULL,
    inspector_officer_id UUID,
    inspection_date     DATE        NOT NULL DEFAULT CURRENT_DATE,
    inspection_type     VARCHAR(30) NOT NULL,
    findings            TEXT,
    result              VARCHAR(30) NOT NULL,
    notes               TEXT,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inspection_declaration ON inspection_records(declaration_code);
CREATE INDEX idx_inspection_result      ON inspection_records(result);
