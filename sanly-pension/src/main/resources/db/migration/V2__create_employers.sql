CREATE TABLE employer_sequence (
    id          BIGINT PRIMARY KEY,
    last_number BIGINT NOT NULL DEFAULT 0
);
INSERT INTO employer_sequence (id, last_number) VALUES (1, 0);

CREATE TABLE employers (
    employer_id                 BIGSERIAL PRIMARY KEY,
    employer_code               VARCHAR(20)  NOT NULL UNIQUE,
    business_name               VARCHAR(300) NOT NULL,
    business_registration_number VARCHAR(50) NOT NULL UNIQUE,
    contact_national_id         VARCHAR(20)  NOT NULL,
    registered_at               TIMESTAMP    NOT NULL DEFAULT now(),
    status                      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX idx_employers_status ON employers(status);
