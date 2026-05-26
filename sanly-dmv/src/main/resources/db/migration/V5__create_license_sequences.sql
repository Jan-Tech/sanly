-- SANLY DMV — V5: Per-year license number sequence counters
-- One row per calendar year; pessimistic-locked during issuance to guarantee unique numbers.

CREATE TABLE IF NOT EXISTS license_sequences (
    year            INTEGER     NOT NULL,
    last_counter    INTEGER     NOT NULL    DEFAULT 0,

    CONSTRAINT pk_license_sequences     PRIMARY KEY (year),
    CONSTRAINT chk_seq_positive         CHECK (last_counter >= 0)
);
