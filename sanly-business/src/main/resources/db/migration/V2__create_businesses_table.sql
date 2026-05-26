CREATE TABLE IF NOT EXISTS businesses (
    business_id         UUID        NOT NULL DEFAULT gen_random_uuid(),
    registration_number VARCHAR(25) NOT NULL UNIQUE,
    business_name       VARCHAR(300) NOT NULL,
    business_type       VARCHAR(25) NOT NULL,
    owner_national_id   VARCHAR(30) NOT NULL,
    registration_date   DATE        NOT NULL,
    address             TEXT,               -- AES-256-GCM encrypted
    phone               VARCHAR(30),
    status              VARCHAR(15) NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('PENDING','ACTIVE','SUSPENDED','DISSOLVED')),
    approved_by_officer_id BIGINT,
    approved_at         TIMESTAMP,
    notes               TEXT,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_businesses PRIMARY KEY (business_id),
    CONSTRAINT fk_biz_officer FOREIGN KEY (approved_by_officer_id)
        REFERENCES registration_officers (officer_id)
);
CREATE INDEX idx_biz_owner  ON businesses (owner_national_id);
CREATE INDEX idx_biz_status ON businesses (status);
CREATE INDEX idx_biz_regnum ON businesses (registration_number);
