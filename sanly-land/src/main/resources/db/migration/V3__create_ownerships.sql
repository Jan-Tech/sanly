CREATE TABLE ownerships (
    ownership_id     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    cadastral_number VARCHAR(30) NOT NULL,
    owner_national_id VARCHAR(20) NOT NULL,
    ownership_share  INT         NOT NULL DEFAULT 100,
    ownership_type   VARCHAR(20) NOT NULL DEFAULT 'SOLE',
    acquired_at      DATE        NOT NULL,
    acquired_via     VARCHAR(20) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ownerships_cadastral ON ownerships(cadastral_number);
CREATE INDEX idx_ownerships_owner     ON ownerships(owner_national_id);
CREATE INDEX idx_ownerships_status    ON ownerships(status);
