CREATE TABLE IF NOT EXISTS ddr_sequences (
    id            BIGSERIAL PRIMARY KEY,
    year          INT UNIQUE NOT NULL,
    last_sequence INT NOT NULL DEFAULT 0
);
