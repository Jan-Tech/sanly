-- Per-year counter for generating TM-TAX-YYYYNNNNNN identifiers.
CREATE TABLE IF NOT EXISTS tax_id_sequences (
    year            INTEGER NOT NULL,
    last_counter    INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT pk_tax_id_sequences  PRIMARY KEY (year),
    CONSTRAINT chk_counter_positive CHECK (last_counter >= 0)
);
