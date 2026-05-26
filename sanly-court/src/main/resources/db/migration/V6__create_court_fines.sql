CREATE TABLE fine_sequences (
    year       INTEGER PRIMARY KEY,
    next_value INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE court_fines (
    fine_id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    fine_code              VARCHAR(30) NOT NULL UNIQUE,
    case_number            VARCHAR(30) NOT NULL,
    citizen_national_id    VARCHAR(20) NOT NULL,
    amount                 TEXT        NOT NULL,
    reason                 TEXT        NOT NULL,
    issued_at              TIMESTAMP   NOT NULL DEFAULT NOW(),
    due_date               DATE        NOT NULL,
    status                 VARCHAR(30) NOT NULL DEFAULT 'OUTSTANDING',
    paid_at                TIMESTAMP,
    payment_proof_note     TEXT,
    notes                  TEXT,
    created_at             TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fine_citizen ON court_fines(citizen_national_id);
CREATE INDEX idx_fine_case    ON court_fines(case_number);
CREATE INDEX idx_fine_status  ON court_fines(status);
CREATE INDEX idx_fine_due     ON court_fines(due_date, status);
