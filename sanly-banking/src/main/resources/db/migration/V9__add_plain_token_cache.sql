-- V9: Add plain_token_cache to consent_requests
-- Stores the plain consent token temporarily after citizen approval.
-- Bank retrieves it once via GET /consent/{code}/status, then it is cleared to NULL.
-- This avoids long-term storage of the plain token while enabling the polling pattern.

ALTER TABLE consent_requests
    ADD COLUMN IF NOT EXISTS plain_token_cache VARCHAR(64);

COMMENT ON COLUMN consent_requests.plain_token_cache IS
    'Plain consent token (32 hex chars). Set on approval, cleared after bank retrieves it via status poll. Never stored permanently.';
