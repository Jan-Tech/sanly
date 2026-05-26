CREATE TABLE pension_accounts (
    account_id               UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    citizen_national_id      VARCHAR(20) NOT NULL UNIQUE,
    contribution_start_date  DATE        NOT NULL,
    total_contributions      TEXT,
    eligible_at              DATE        NOT NULL,
    status                   VARCHAR(20) NOT NULL DEFAULT 'ACCUMULATING',
    created_at               TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pension_eligible ON pension_accounts(eligible_at, status);
