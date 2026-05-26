CREATE TABLE availability_exceptions (
    exception_id BIGSERIAL PRIMARY KEY,
    office_code VARCHAR(12) NOT NULL,
    exception_date DATE NOT NULL,
    reason VARCHAR(20) NOT NULL DEFAULT 'PUBLIC_HOLIDAY',
    is_closed BOOLEAN NOT NULL DEFAULT TRUE,
    alternate_open_time TIME,
    alternate_close_time TIME
);

CREATE INDEX idx_exc_office_date ON availability_exceptions (office_code, exception_date);
