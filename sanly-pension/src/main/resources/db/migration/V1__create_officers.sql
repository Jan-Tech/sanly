CREATE TABLE pension_officers (
    officer_id    BIGSERIAL PRIMARY KEY,
    national_id   VARCHAR(20)  NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    region        VARCHAR(100),
    role          VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    employer_code VARCHAR(20)
);
