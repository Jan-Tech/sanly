CREATE TABLE tracking_updates (
    update_id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tracking_code      VARCHAR(22) NOT NULL,
    status             VARCHAR(50) NOT NULL,
    description        VARCHAR(1000),
    updated_at         TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_by_service VARCHAR(50)
);

CREATE INDEX idx_tupdate_code ON tracking_updates (tracking_code);
