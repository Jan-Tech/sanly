CREATE TABLE education_institutions (
    institution_id    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_code  VARCHAR(20)  NOT NULL UNIQUE,
    name              VARCHAR(255) NOT NULL,
    type              VARCHAR(30)  NOT NULL,
    region            VARCHAR(100) NOT NULL,
    address           TEXT         NOT NULL,
    license_number    VARCHAR(50),
    accredited_until  DATE,
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE SEQUENCE edu_institution_seq START 1;
