CREATE TABLE benefit_programs (
    program_id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    program_code         VARCHAR(20)  NOT NULL UNIQUE,
    name                 VARCHAR(255) NOT NULL,
    description          TEXT,
    benefit_type         VARCHAR(30)  NOT NULL,
    monthly_amount       TEXT         NOT NULL,
    eligibility_criteria TEXT,
    max_duration_months  INT,
    status               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_programs_type   ON benefit_programs(benefit_type);
CREATE INDEX idx_programs_status ON benefit_programs(status);
