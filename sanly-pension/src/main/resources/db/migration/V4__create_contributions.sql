CREATE TABLE contribution_records (
    contribution_id       BIGSERIAL PRIMARY KEY,
    account_code          VARCHAR(25) NOT NULL,
    employer_code         VARCHAR(20),
    contribution_month    VARCHAR(7)  NOT NULL,
    employer_amount       VARCHAR(512),
    citizen_amount        VARCHAR(512),
    total_amount          VARCHAR(512) NOT NULL,
    submitted_at          TIMESTAMP   NOT NULL DEFAULT now(),
    submitted_by_officer_id BIGINT,
    status                VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    rejection_reason      VARCHAR(500)
);

CREATE INDEX idx_contributions_account ON contribution_records(account_code);
CREATE INDEX idx_contributions_employer ON contribution_records(employer_code);
CREATE INDEX idx_contributions_month   ON contribution_records(contribution_month);
CREATE INDEX idx_contributions_status  ON contribution_records(status);
CREATE UNIQUE INDEX idx_contributions_unique_month
    ON contribution_records(account_code, employer_code, contribution_month)
    WHERE status != 'REJECTED';
