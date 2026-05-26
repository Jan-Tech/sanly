CREATE TABLE IF NOT EXISTS child_benefit_records (
    benefit_id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    child_national_id   VARCHAR(11) NOT NULL,
    mother_national_id  VARCHAR(11),
    father_national_id  VARCHAR(11),
    birth_date          DATE        NOT NULL,
    status              VARCHAR(15) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    cancelled_at        TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cbr_child  ON child_benefit_records (child_national_id);
CREATE INDEX IF NOT EXISTS idx_cbr_mother ON child_benefit_records (mother_national_id);
CREATE INDEX IF NOT EXISTS idx_cbr_father ON child_benefit_records (father_national_id);
CREATE INDEX IF NOT EXISTS idx_cbr_status ON child_benefit_records (status);
