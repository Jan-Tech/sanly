CREATE TABLE academic_records (
    record_id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    citizen_national_id   VARCHAR(20) NOT NULL,
    institution_code      VARCHAR(20) NOT NULL,
    academic_year         VARCHAR(20) NOT NULL,
    grade                 TEXT,
    gpa                   TEXT,
    notes                 TEXT,
    recorded_by_officer_id UUID,
    created_at            TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_acad_records_citizen ON academic_records(citizen_national_id);
CREATE INDEX idx_acad_records_institution ON academic_records(institution_code);
