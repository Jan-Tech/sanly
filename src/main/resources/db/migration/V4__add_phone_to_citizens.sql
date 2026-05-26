-- V4: Add encrypted phone number field to citizens table.
-- Existing citizens will have phone_number = NULL until registered at a government office.
ALTER TABLE citizens ADD COLUMN IF NOT EXISTS phone_number TEXT;
