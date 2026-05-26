CREATE TABLE marriage_records (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    certificate_number      VARCHAR(25) NOT NULL UNIQUE,
    spouse1_national_id     VARCHAR(11) NOT NULL,
    spouse1_full_name       VARCHAR(255) NOT NULL,
    spouse2_national_id     VARCHAR(11) NOT NULL,
    spouse2_full_name       VARCHAR(255) NOT NULL,
    marriage_date           DATE        NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    dissolution_date        DATE,
    registering_officer_id  BIGINT      REFERENCES civil_officers(id),
    bridge_published        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
