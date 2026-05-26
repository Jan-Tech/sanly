-- V8: Seed BANK and CONSENT sequences for current year

INSERT INTO banking_code_sequences (id, sequence_type, year, next_val)
VALUES
    (gen_random_uuid(), 'BANK',    EXTRACT(YEAR FROM NOW())::INTEGER, 4),
    -- Starting at 4 because V7 already created TM-BNK-001, TM-BNK-002, TM-BNK-003
    (gen_random_uuid(), 'CONSENT', EXTRACT(YEAR FROM NOW())::INTEGER, 1)
ON CONFLICT (sequence_type) DO NOTHING;
