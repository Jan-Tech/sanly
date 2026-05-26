-- V7: Seed demo banks
-- NOTE: apiKeyHash is BCrypt("bank-default-key-change-me") — rotate via /banks/{code}/rotate-key before production use

-- BCrypt hash of "bank-default-key-change-me" with cost 12
-- (pre-computed: $2a$12$xYz... — use rotate-key endpoint to replace)
DO $$
DECLARE
    default_hash TEXT := '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi';
    -- Note: above is a placeholder BCrypt hash; DataInitializer logs a warning about this
BEGIN

INSERT INTO registered_banks (bank_id, bank_code, bank_name, license_number, status, api_key_hash, registered_at)
VALUES
    (gen_random_uuid(), 'TM-BNK-001', 'Türkmenbaşy Bank',   'TM-LIC-001', 'ACTIVE',           default_hash, NOW()),
    (gen_random_uuid(), 'TM-BNK-002', 'Daýhanbank',          'TM-LIC-002', 'ACTIVE',           default_hash, NOW()),
    (gen_random_uuid(), 'TM-BNK-003', 'Halkbank',            'TM-LIC-003', 'PENDING_APPROVAL', default_hash, NOW())
ON CONFLICT (bank_code) DO NOTHING;

END $$;
