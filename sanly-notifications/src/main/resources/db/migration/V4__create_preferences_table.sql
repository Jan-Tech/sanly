CREATE TABLE IF NOT EXISTS notification_preferences (
    preference_id       UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    citizen_national_id VARCHAR(30) NOT NULL,
    event_type          VARCHAR(60) NOT NULL,
    email_enabled       BOOLEAN     NOT NULL DEFAULT TRUE,
    sms_enabled         BOOLEAN     NOT NULL DEFAULT FALSE,
    in_app_enabled      BOOLEAN     NOT NULL DEFAULT TRUE,
    enabled             BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_pref_nin_event UNIQUE (citizen_national_id, event_type)
);

CREATE INDEX IF NOT EXISTS idx_pref_nin ON notification_preferences (citizen_national_id);
