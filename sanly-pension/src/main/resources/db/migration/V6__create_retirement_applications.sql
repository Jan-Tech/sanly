CREATE TABLE retirement_applications (
    application_id          BIGSERIAL PRIMARY KEY,
    account_code            VARCHAR(25)  NOT NULL,
    citizen_national_id     VARCHAR(20)  NOT NULL,
    applied_at              TIMESTAMP    NOT NULL DEFAULT now(),
    requested_start_date    DATE         NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    processed_by_officer_id BIGINT,
    processed_at            TIMESTAMP,
    rejection_reason        VARCHAR(500),
    monthly_pension_amount  VARCHAR(512)
);

CREATE INDEX idx_retirement_account ON retirement_applications(account_code);
CREATE INDEX idx_retirement_nin     ON retirement_applications(citizen_national_id);
CREATE INDEX idx_retirement_status  ON retirement_applications(status);
