CREATE TABLE benefit_payments (
    payment_id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_code          VARCHAR(30) NOT NULL,
    citizen_national_id VARCHAR(20) NOT NULL,
    payment_period      VARCHAR(7)  NOT NULL, -- YYYY-MM
    amount              TEXT        NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    scheduled_date      DATE        NOT NULL,
    paid_at             TIMESTAMP,
    notes               TEXT,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_claim    ON benefit_payments(claim_code);
CREATE INDEX idx_payments_citizen  ON benefit_payments(citizen_national_id);
CREATE INDEX idx_payments_period   ON benefit_payments(payment_period);
CREATE INDEX idx_payments_status   ON benefit_payments(status);
CREATE UNIQUE INDEX idx_payments_claim_period ON benefit_payments(claim_code, payment_period);
