-- SANLY Bridge — V3: Exchange audit log
-- Immutable. Every data exchange (query or publish) is recorded here.
-- No updates or deletes are ever performed on this table.

CREATE TABLE IF NOT EXISTS exchange_logs (
    id                  BIGSERIAL       NOT NULL,
    requesting_code     VARCHAR(50)     NOT NULL,
    target_code         VARCHAR(50),                -- NULL for PUBLISH operations
    national_id         VARCHAR(30),                -- citizen this exchange concerns
    data_type           VARCHAR(50)     NOT NULL,
    operation_type      VARCHAR(10)     NOT NULL    -- 'QUERY' or 'PUBLISH'
                            CHECK (operation_type IN ('QUERY', 'PUBLISH')),
    result              VARCHAR(15)     NOT NULL
                            CHECK (result IN ('SUCCESS', 'DENIED', 'NOT_FOUND', 'ERROR')),
    response_time_ms    INTEGER,
    details             TEXT,                       -- denial reason, error message, etc.
    exchanged_at        TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_exchange_logs PRIMARY KEY (id)
);

CREATE INDEX idx_exlog_requesting ON exchange_logs (requesting_code);
CREATE INDEX idx_exlog_national_id ON exchange_logs (national_id);
CREATE INDEX idx_exlog_data_type   ON exchange_logs (data_type);
CREATE INDEX idx_exlog_result      ON exchange_logs (result);
CREATE INDEX idx_exlog_exchanged_at ON exchange_logs (exchanged_at DESC);
