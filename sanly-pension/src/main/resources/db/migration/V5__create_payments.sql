CREATE TABLE pension_payments (
    payment_id          BIGSERIAL PRIMARY KEY,
    account_code        VARCHAR(25) NOT NULL,
    citizen_national_id VARCHAR(20) NOT NULL,
    payment_month       VARCHAR(7)  NOT NULL,
    amount              VARCHAR(512) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    scheduled_date      DATE        NOT NULL,
    paid_at             TIMESTAMP,
    notes               VARCHAR(500),
    UNIQUE(account_code, payment_month)
);

CREATE INDEX idx_payments_account ON pension_payments(account_code);
CREATE INDEX idx_payments_status  ON pension_payments(status);
CREATE INDEX idx_payments_month   ON pension_payments(payment_month);
