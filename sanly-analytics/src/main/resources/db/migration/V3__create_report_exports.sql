CREATE TABLE IF NOT EXISTS report_exports (
    export_id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    export_code             VARCHAR(30) NOT NULL,
    report_type             VARCHAR(50) NOT NULL,
    generated_by_officer_id UUID,
    date_from               DATE,
    date_to                 DATE,
    parameters              TEXT,
    status                  VARCHAR(20) NOT NULL DEFAULT 'GENERATING',
    report_data             BYTEA,
    report_data_csv         BYTEA,
    failure_reason          TEXT,
    generated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at            TIMESTAMPTZ,
    expires_at              TIMESTAMPTZ,
    CONSTRAINT uq_export_code UNIQUE (export_code)
);

CREATE INDEX IF NOT EXISTS idx_exports_status ON report_exports (status);
CREATE INDEX IF NOT EXISTS idx_exports_officer ON report_exports (generated_by_officer_id);
CREATE INDEX IF NOT EXISTS idx_exports_generated_at ON report_exports (generated_at DESC);
