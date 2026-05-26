CREATE TABLE diploma_sequences (
    year       INT  PRIMARY KEY,
    next_value BIGINT NOT NULL DEFAULT 1
);

CREATE TABLE diplomas (
    diploma_id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    diploma_code           VARCHAR(30) NOT NULL UNIQUE,
    citizen_national_id    VARCHAR(20) NOT NULL,
    institution_code       VARCHAR(20) NOT NULL,
    program_name           VARCHAR(255) NOT NULL,
    program_level          VARCHAR(30) NOT NULL,
    graduation_date        DATE        NOT NULL,
    honors                 VARCHAR(30) NOT NULL DEFAULT 'NONE',
    issued_by_officer_id   UUID,
    issued_at              TIMESTAMP   NOT NULL DEFAULT NOW(),
    status                 VARCHAR(20) NOT NULL DEFAULT 'VALID',
    revoked_reason         TEXT,
    bridge_published       BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_diplomas_citizen ON diplomas(citizen_national_id);
CREATE INDEX idx_diplomas_institution ON diplomas(institution_code);
CREATE INDEX idx_diplomas_status ON diplomas(status);
