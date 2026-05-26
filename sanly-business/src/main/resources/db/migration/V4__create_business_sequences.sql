CREATE TABLE IF NOT EXISTS business_sequences (
    year            INTEGER NOT NULL,
    last_counter    INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT pk_business_sequences  PRIMARY KEY (year),
    CONSTRAINT chk_biz_seq_positive   CHECK (last_counter >= 0)
);
