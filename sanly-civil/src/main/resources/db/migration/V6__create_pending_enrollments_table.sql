CREATE TABLE IF NOT EXISTS pending_enrollments (
    enrollment_id       UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    child_national_id   VARCHAR(11) NOT NULL,
    mother_national_id  VARCHAR(11),
    father_national_id  VARCHAR(11),
    child_full_name     VARCHAR(300),
    expected_school_year INTEGER    NOT NULL,
    status              VARCHAR(15) NOT NULL DEFAULT 'QUEUED',
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    notified_at         TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pe_child       ON pending_enrollments (child_national_id);
CREATE INDEX IF NOT EXISTS idx_pe_school_year ON pending_enrollments (expected_school_year);
CREATE INDEX IF NOT EXISTS idx_pe_status      ON pending_enrollments (status);
