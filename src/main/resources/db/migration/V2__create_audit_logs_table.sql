-- SANLY Citizen Registry — V2: Audit log table
-- Records every access or modification to citizen records.

CREATE TABLE IF NOT EXISTS audit_logs (
    id                  BIGSERIAL       NOT NULL,
    citizen_national_id CHAR(11),                  -- NULL for search/bulk operations
    action              VARCHAR(60)     NOT NULL,
    performed_by        VARCHAR(200)    NOT NULL,
    role                VARCHAR(50)     NOT NULL,
    ip_address          VARCHAR(45),               -- IPv4 or IPv6
    endpoint            VARCHAR(300),
    http_method         VARCHAR(10),
    status_code         SMALLINT,
    details             TEXT,
    accessed_at         TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

CREATE INDEX idx_audit_citizen_id   ON audit_logs (citizen_national_id);
CREATE INDEX idx_audit_accessed_at  ON audit_logs (accessed_at DESC);
CREATE INDEX idx_audit_performed_by ON audit_logs (performed_by);
CREATE INDEX idx_audit_action       ON audit_logs (action);
