CREATE TABLE IF NOT EXISTS service_usage_stats (
    stat_id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    stat_date       DATE        NOT NULL,
    service_name    VARCHAR(50) NOT NULL,
    endpoint        VARCHAR(200),
    request_count   BIGINT      NOT NULL DEFAULT 0,
    avg_response_ms DOUBLE PRECISION     DEFAULT 0.0,
    error_count     BIGINT      NOT NULL DEFAULT 0,
    error_rate      DOUBLE PRECISION     DEFAULT 0.0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usage_date_service_endpoint UNIQUE (stat_date, service_name, endpoint)
);

CREATE INDEX IF NOT EXISTS idx_usage_date ON service_usage_stats (stat_date DESC);
CREATE INDEX IF NOT EXISTS idx_usage_service ON service_usage_stats (service_name);

CREATE TABLE IF NOT EXISTS regional_stats (
    stat_id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    stat_date            DATE        NOT NULL,
    region               VARCHAR(100) NOT NULL,
    citizen_count        BIGINT      NOT NULL DEFAULT 0,
    business_count       BIGINT      NOT NULL DEFAULT 0,
    property_count       BIGINT      NOT NULL DEFAULT 0,
    appointment_count    BIGINT      NOT NULL DEFAULT 0,
    benefit_claim_count  BIGINT      NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_regional_date_region UNIQUE (stat_date, region)
);

CREATE INDEX IF NOT EXISTS idx_regional_date ON regional_stats (stat_date DESC);

CREATE TABLE IF NOT EXISTS anomaly_trends (
    trend_id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    stat_date             DATE        NOT NULL,
    institution_code      VARCHAR(50) NOT NULL,
    alert_type            VARCHAR(50) NOT NULL,
    alert_count           BIGINT      NOT NULL DEFAULT 0,
    resolved_count        BIGINT      NOT NULL DEFAULT 0,
    avg_resolution_hours  DOUBLE PRECISION     DEFAULT 0.0,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_anomaly_date_inst_type UNIQUE (stat_date, institution_code, alert_type)
);

CREATE INDEX IF NOT EXISTS idx_anomaly_date ON anomaly_trends (stat_date DESC);
