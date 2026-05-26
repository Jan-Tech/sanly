-- SANLY Medical — V2: Doctors (also serve as system user accounts)
-- clinic_id and national_id are nullable to support admin-only accounts
-- that are not associated with any clinic.

CREATE TABLE IF NOT EXISTS doctors (
    doctor_id       BIGSERIAL       NOT NULL,
    national_id     CHAR(11),                   -- their own TM-NIN; null for admin accounts
    first_name      VARCHAR(150)    NOT NULL,
    last_name       VARCHAR(150)    NOT NULL,
    specialization  VARCHAR(200),
    license_number  VARCHAR(100),
    clinic_id       BIGINT,                     -- null for admin accounts
    username        VARCHAR(100)    NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_doctors               PRIMARY KEY (doctor_id),
    CONSTRAINT uq_doctors_username      UNIQUE (username),
    CONSTRAINT uq_doctors_national_id   UNIQUE (national_id),
    CONSTRAINT uq_doctors_license       UNIQUE (license_number),
    CONSTRAINT fk_doctors_clinic        FOREIGN KEY (clinic_id)
        REFERENCES clinics (clinic_id)
);

CREATE TABLE IF NOT EXISTS doctor_roles (
    doctor_id   BIGINT      NOT NULL,
    role        VARCHAR(30) NOT NULL,

    CONSTRAINT pk_doctor_roles PRIMARY KEY (doctor_id, role),
    CONSTRAINT fk_doctor_roles FOREIGN KEY (doctor_id)
        REFERENCES doctors (doctor_id) ON DELETE CASCADE
);

CREATE INDEX idx_doctors_clinic_id ON doctors (clinic_id);
CREATE INDEX idx_doctors_status    ON doctors (status);
