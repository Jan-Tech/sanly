CREATE TABLE IF NOT EXISTS criminal_records (
    record_id               UUID            NOT NULL    DEFAULT gen_random_uuid(),
    citizen_national_id     VARCHAR(30)     NOT NULL,
    offense_type            VARCHAR(30)     NOT NULL,
    offense_date            DATE            NOT NULL,
    verdict                 VARCHAR(20)     NOT NULL,
    sentence_description    TEXT,                       -- AES-256-GCM encrypted
    court_name              VARCHAR(200),
    recorded_by_officer_id  BIGINT          NOT NULL,
    created_at              TIMESTAMP       NOT NULL    DEFAULT NOW(),
    expires_at              TIMESTAMP,
    status                  VARCHAR(15)     NOT NULL    DEFAULT 'ACTIVE'
                                CHECK (status IN ('ACTIVE','EXPUNGED')),
    bridge_published        BOOLEAN         NOT NULL    DEFAULT FALSE,
    CONSTRAINT pk_criminal_records PRIMARY KEY (record_id),
    CONSTRAINT fk_cr_officer FOREIGN KEY (recorded_by_officer_id)
        REFERENCES police_officers (officer_id)
);

CREATE INDEX idx_cr_citizen     ON criminal_records (citizen_national_id);
CREATE INDEX idx_cr_verdict     ON criminal_records (verdict);
CREATE INDEX idx_cr_status      ON criminal_records (status);
CREATE INDEX idx_cr_offense_dt  ON criminal_records (offense_date DESC);
