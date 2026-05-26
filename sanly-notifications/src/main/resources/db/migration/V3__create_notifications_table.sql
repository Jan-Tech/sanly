CREATE TABLE IF NOT EXISTS notifications (
    notification_id     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    citizen_national_id VARCHAR(30) NOT NULL,
    event_type          VARCHAR(60) NOT NULL,
    title               VARCHAR(200) NOT NULL,
    body                TEXT        NOT NULL,
    channel             VARCHAR(20) NOT NULL,
    language            VARCHAR(5)  NOT NULL DEFAULT 'EN',
    status              VARCHAR(15) NOT NULL DEFAULT 'UNREAD',
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    read_at             TIMESTAMP,
    delivered_at        TIMESTAMP,
    metadata            TEXT
);

CREATE INDEX IF NOT EXISTS idx_notif_nin     ON notifications (citizen_national_id);
CREATE INDEX IF NOT EXISTS idx_notif_status  ON notifications (status);
CREATE INDEX IF NOT EXISTS idx_notif_created ON notifications (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notif_event   ON notifications (event_type);
