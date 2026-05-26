CREATE TABLE enrollments (
    enrollment_id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    citizen_national_id      VARCHAR(20) NOT NULL,
    institution_code         VARCHAR(20) NOT NULL,
    enrollment_date          DATE        NOT NULL,
    expected_graduation_year INT,
    program_name             VARCHAR(255),
    status                   VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at               TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_enrollments_citizen ON enrollments(citizen_national_id);
CREATE INDEX idx_enrollments_institution ON enrollments(institution_code);
