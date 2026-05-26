-- SANLY Bridge — V6: Purpose codes on every query (Anti-Corruption Feature 1)
-- Every exchange query must now declare a purpose code. Citizens can see
-- the purposeCode and caseReference when they view their own access log,
-- giving full transparency into WHY their data was accessed.

ALTER TABLE exchange_logs
    ADD COLUMN IF NOT EXISTS purpose_code    VARCHAR(50),
    ADD COLUMN IF NOT EXISTS case_reference  VARCHAR(100),
    ADD COLUMN IF NOT EXISTS justification   TEXT;

-- Index for filtering/analysis by purpose code
CREATE INDEX IF NOT EXISTS idx_exlog_purpose ON exchange_logs (purpose_code)
    WHERE purpose_code IS NOT NULL;
