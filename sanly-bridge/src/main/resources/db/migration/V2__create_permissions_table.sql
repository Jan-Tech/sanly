-- SANLY Bridge — V2: Inter-institution query permissions
-- Each row grants institution A the right to query data type D from institution B.
-- Soft-deleted via active=false (never hard-deleted, for audit purposes).

CREATE TABLE IF NOT EXISTS institution_permissions (
    id                  BIGSERIAL       NOT NULL,
    requesting_code     VARCHAR(50)     NOT NULL,
    target_code         VARCHAR(50)     NOT NULL,
    data_type           VARCHAR(50)     NOT NULL,
    granted_by          VARCHAR(200),
    granted_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    active              BOOLEAN         NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_inst_permissions PRIMARY KEY (id),
    CONSTRAINT uq_inst_permission  UNIQUE (requesting_code, target_code, data_type),
    CONSTRAINT fk_perm_requesting  FOREIGN KEY (requesting_code)
        REFERENCES institutions (institution_code),
    CONSTRAINT fk_perm_target      FOREIGN KEY (target_code)
        REFERENCES institutions (institution_code)
);

CREATE INDEX idx_perm_requesting ON institution_permissions (requesting_code);
CREATE INDEX idx_perm_target     ON institution_permissions (target_code);
CREATE INDEX idx_perm_active     ON institution_permissions (active);
