CREATE TABLE death_records (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    certificate_number      VARCHAR(25) NOT NULL UNIQUE,
    deceased_national_id    VARCHAR(11) NOT NULL UNIQUE,
    deceased_full_name      VARCHAR(255) NOT NULL,
    date_of_death           DATE        NOT NULL,
    place_of_death          VARCHAR(255) NOT NULL,
    death_cause             TEXT,
    registering_officer_id  BIGINT      REFERENCES civil_officers(id),
    bridge_published        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
