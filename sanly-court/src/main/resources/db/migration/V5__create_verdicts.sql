CREATE TABLE verdicts (
    verdict_id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    case_number             VARCHAR(30) NOT NULL UNIQUE,
    verdict_type            VARCHAR(30) NOT NULL,
    summary                 TEXT,
    issued_by_judge_officer_id UUID,
    issued_at               TIMESTAMP   NOT NULL DEFAULT NOW(),
    appeal_deadline         DATE        NOT NULL,
    appealed                BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_verdict_type    ON verdicts(verdict_type);
CREATE INDEX idx_verdict_issued  ON verdicts(issued_at);
