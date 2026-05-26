CREATE TABLE birth_records (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    certificate_number  VARCHAR(25)  NOT NULL UNIQUE,
    child_national_id   VARCHAR(11)  NOT NULL UNIQUE,
    child_first_name    VARCHAR(100) NOT NULL,
    child_last_name     VARCHAR(100) NOT NULL,
    date_of_birth       DATE         NOT NULL,
    place_of_birth      VARCHAR(255) NOT NULL,
    father_national_id  VARCHAR(11),
    father_full_name    VARCHAR(255),
    mother_national_id  VARCHAR(11),
    mother_full_name    VARCHAR(255),
    registering_officer_id BIGINT   REFERENCES civil_officers(id),
    bridge_published    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
