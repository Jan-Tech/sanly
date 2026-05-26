CREATE TABLE court_officers (
    officer_id   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    national_id  VARCHAR(20)  NOT NULL UNIQUE,
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    username     VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    court_code   VARCHAR(20),
    role         VARCHAR(20)  NOT NULL DEFAULT 'CLERK',
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_officer_court ON court_officers(court_code);
