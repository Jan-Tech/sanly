CREATE TABLE case_sequences (
    year       INTEGER PRIMARY KEY,
    next_value INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE court_cases (
    case_id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    case_number               VARCHAR(30) NOT NULL UNIQUE,
    court_code                VARCHAR(20) NOT NULL,
    case_type                 VARCHAR(30) NOT NULL,
    plaintiff_national_id     VARCHAR(20) NOT NULL,
    defendant_national_id     VARCHAR(20) NOT NULL,
    assigned_judge_officer_id UUID,
    filed_at                  TIMESTAMP   NOT NULL DEFAULT NOW(),
    hearing_date              DATE,
    closed_at                 TIMESTAMP,
    status                    VARCHAR(30) NOT NULL DEFAULT 'FILED',
    summary                   TEXT,
    notes                     TEXT,
    created_at                TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_case_court       ON court_cases(court_code);
CREATE INDEX idx_case_plaintiff   ON court_cases(plaintiff_national_id);
CREATE INDEX idx_case_defendant   ON court_cases(defendant_national_id);
CREATE INDEX idx_case_status      ON court_cases(status);
CREATE INDEX idx_case_hearing     ON court_cases(hearing_date) WHERE hearing_date IS NOT NULL;
