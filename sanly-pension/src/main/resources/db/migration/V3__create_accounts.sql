CREATE TABLE account_sequence (
    id          BIGINT PRIMARY KEY,
    last_number BIGINT NOT NULL DEFAULT 0
);
INSERT INTO account_sequence (id, last_number) VALUES (1, 0);

CREATE TABLE pension_accounts (
    account_id                   BIGSERIAL PRIMARY KEY,
    account_code                 VARCHAR(25) NOT NULL UNIQUE,
    citizen_national_id          VARCHAR(20) NOT NULL UNIQUE,
    opened_at                    TIMESTAMP   NOT NULL DEFAULT now(),
    employment_start_date        DATE        NOT NULL,
    birth_date                   DATE        NOT NULL,
    gender                       CHAR(1)     NOT NULL,
    retirement_age_target        INTEGER     NOT NULL,
    eligible_at                  DATE        NOT NULL,
    total_contributions          VARCHAR(512),
    total_employer_contributions VARCHAR(512),
    total_citizen_contributions  VARCHAR(512),
    status                       VARCHAR(20) NOT NULL DEFAULT 'ACCUMULATING'
);

CREATE INDEX idx_pension_accounts_nin    ON pension_accounts(citizen_national_id);
CREATE INDEX idx_pension_accounts_status ON pension_accounts(status);
CREATE INDEX idx_pension_accounts_eligible ON pension_accounts(eligible_at) WHERE status = 'ACCUMULATING';
