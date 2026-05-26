CREATE TABLE courts (
    court_id    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    court_code  VARCHAR(20)  NOT NULL UNIQUE,
    name        VARCHAR(200) NOT NULL,
    court_type  VARCHAR(30)  NOT NULL,
    region      VARCHAR(100),
    address     VARCHAR(300),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_court_type   ON courts(court_type);
CREATE INDEX idx_court_status ON courts(status);
