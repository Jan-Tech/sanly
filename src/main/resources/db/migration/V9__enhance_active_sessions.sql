-- Add device name and last action tracking to active_sessions
ALTER TABLE active_sessions
    ADD COLUMN IF NOT EXISTS device_name  VARCHAR(100),
    ADD COLUMN IF NOT EXISTS last_action  VARCHAR(100);
