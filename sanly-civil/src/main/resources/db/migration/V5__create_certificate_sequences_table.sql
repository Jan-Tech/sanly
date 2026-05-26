CREATE TABLE certificate_sequences (
    seq_type   VARCHAR(10) NOT NULL,
    year       INTEGER     NOT NULL,
    last_value BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (seq_type, year)
);
