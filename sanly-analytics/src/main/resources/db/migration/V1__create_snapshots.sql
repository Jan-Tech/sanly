CREATE TABLE IF NOT EXISTS export_code_sequences (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    year        INT         NOT NULL,
    next_val    BIGINT      NOT NULL DEFAULT 1,
    CONSTRAINT uq_export_seq_year UNIQUE (year)
);

CREATE TABLE IF NOT EXISTS daily_snapshots (
    snapshot_id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    snapshot_date           DATE        NOT NULL,

    -- Citizens
    total_citizens          BIGINT      NOT NULL DEFAULT 0,
    active_citizens         BIGINT      NOT NULL DEFAULT 0,
    deceased_citizens       BIGINT      NOT NULL DEFAULT 0,
    new_registrations_today BIGINT      NOT NULL DEFAULT 0,

    -- Businesses
    total_businesses        BIGINT      NOT NULL DEFAULT 0,
    active_businesses       BIGINT      NOT NULL DEFAULT 0,
    new_businesses_today    BIGINT      NOT NULL DEFAULT 0,

    -- Licenses (DMV)
    total_licenses          BIGINT      NOT NULL DEFAULT 0,
    licenses_issued_today   BIGINT      NOT NULL DEFAULT 0,

    -- Education diplomas
    total_diplomas          BIGINT      NOT NULL DEFAULT 0,
    diplomas_issued_today   BIGINT      NOT NULL DEFAULT 0,

    -- Land properties
    total_properties        BIGINT      NOT NULL DEFAULT 0,
    transfers_today         BIGINT      NOT NULL DEFAULT 0,

    -- Social benefits
    total_benefit_claims    BIGINT      NOT NULL DEFAULT 0,
    active_claimants        BIGINT      NOT NULL DEFAULT 0,

    -- Court
    total_court_cases       BIGINT      NOT NULL DEFAULT 0,
    open_cases              BIGINT      NOT NULL DEFAULT 0,

    -- Customs
    total_customs_decls     BIGINT      NOT NULL DEFAULT 0,
    clearances_today        BIGINT      NOT NULL DEFAULT 0,

    -- Appointments
    total_appointments      BIGINT      NOT NULL DEFAULT 0,
    appointments_today      BIGINT      NOT NULL DEFAULT 0,
    no_show_count           BIGINT      NOT NULL DEFAULT 0,
    avg_appointment_rating  DOUBLE PRECISION     DEFAULT 0.0,

    -- Bridge
    total_bridge_exchanges  BIGINT      NOT NULL DEFAULT 0,
    exchanges_today         BIGINT      NOT NULL DEFAULT 0,

    -- Anomalies
    total_anomaly_alerts    BIGINT      NOT NULL DEFAULT 0,
    open_anomaly_alerts     BIGINT      NOT NULL DEFAULT 0,

    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_snapshot_date UNIQUE (snapshot_date)
);

CREATE INDEX IF NOT EXISTS idx_snapshots_date ON daily_snapshots (snapshot_date DESC);
