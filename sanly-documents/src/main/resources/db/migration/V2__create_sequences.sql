CREATE TABLE certificate_sequences (
    id            BIGSERIAL PRIMARY KEY,
    year          INT UNIQUE NOT NULL,
    last_sequence INT        NOT NULL DEFAULT 0
);

CREATE TABLE tracking_sequences (
    id            BIGSERIAL PRIMARY KEY,
    year          INT UNIQUE NOT NULL,
    last_sequence INT        NOT NULL DEFAULT 0
);
