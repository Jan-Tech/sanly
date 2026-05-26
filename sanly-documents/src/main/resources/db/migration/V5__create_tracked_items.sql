CREATE TABLE tracked_items (
    tracking_id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tracking_code       VARCHAR(22)  UNIQUE NOT NULL,
    citizen_national_id VARCHAR(11)  NOT NULL,
    item_type           VARCHAR(30)  NOT NULL,
    source_service      VARCHAR(20)  NOT NULL,
    source_item_code    VARCHAR(50)  UNIQUE NOT NULL,
    title               VARCHAR(300),
    current_status      VARCHAR(50),
    status_description  VARCHAR(1000),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMP,
    is_completed        BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_track_national_id ON tracked_items (citizen_national_id);
CREATE INDEX idx_track_completed   ON tracked_items (is_completed);
CREATE INDEX idx_track_source      ON tracked_items (source_item_code);
