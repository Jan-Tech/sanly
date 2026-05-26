CREATE TABLE land_officers (
    officer_id   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    national_id  VARCHAR(20)  NOT NULL UNIQUE,
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    region       VARCHAR(100),
    role         VARCHAR(20)  NOT NULL DEFAULT 'OFFICER',
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
