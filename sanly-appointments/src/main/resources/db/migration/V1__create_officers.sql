CREATE TABLE appointment_officers (
    officer_id BIGSERIAL PRIMARY KEY,
    national_id VARCHAR(11),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    office_code VARCHAR(12),
    role VARCHAR(10) NOT NULL DEFAULT 'OFFICER',
    status VARCHAR(15) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
