INSERT INTO export_code_sequences (year, next_val)
VALUES (EXTRACT(YEAR FROM NOW())::INT, 1)
ON CONFLICT (year) DO NOTHING;
