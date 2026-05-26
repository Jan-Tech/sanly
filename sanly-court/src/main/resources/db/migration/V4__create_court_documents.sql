CREATE TABLE document_sequences (
    year       INTEGER PRIMARY KEY,
    next_value INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE court_documents (
    document_id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    document_code          VARCHAR(30) NOT NULL UNIQUE,
    case_number            VARCHAR(30) NOT NULL,
    document_type          VARCHAR(30) NOT NULL,
    title                  VARCHAR(300) NOT NULL,
    content                TEXT        NOT NULL,
    submitted_by_national_id VARCHAR(20) NOT NULL,
    submitted_at           TIMESTAMP   NOT NULL DEFAULT NOW(),
    status                 VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    digital_signature      VARCHAR(64) NOT NULL,
    created_at             TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_doc_case   ON court_documents(case_number);
CREATE INDEX idx_doc_status ON court_documents(status);
CREATE INDEX idx_doc_submitter ON court_documents(submitted_by_national_id);
