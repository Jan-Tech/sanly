-- V1: Banking code sequences table
-- Used for generating sequential codes: TM-BNK-NNN, TM-CNS-YYYYNNNNNN

CREATE TABLE IF NOT EXISTS banking_code_sequences (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    sequence_type VARCHAR(20) NOT NULL,
    year          INTEGER     NOT NULL DEFAULT 0,
    next_val      BIGINT      NOT NULL DEFAULT 1,

    CONSTRAINT pk_banking_code_sequences PRIMARY KEY (id),
    CONSTRAINT uq_banking_code_sequences_type UNIQUE (sequence_type)
);

COMMENT ON TABLE  banking_code_sequences IS 'Sequential code generators for banking entities';
COMMENT ON COLUMN banking_code_sequences.sequence_type IS 'BANK or CONSENT';
COMMENT ON COLUMN banking_code_sequences.year           IS 'Current year (for year-based reset of CONSENT sequence)';
COMMENT ON COLUMN banking_code_sequences.next_val       IS 'Next value to use (incremented atomically via PESSIMISTIC_WRITE)';
