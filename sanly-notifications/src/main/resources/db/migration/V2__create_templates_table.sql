CREATE TABLE IF NOT EXISTS notification_templates (
    template_id    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type     VARCHAR(60)  NOT NULL,
    title_template VARCHAR(200) NOT NULL,
    body_template  TEXT         NOT NULL,
    channel        VARCHAR(20)  NOT NULL DEFAULT 'ALL',
    language       VARCHAR(5)   NOT NULL DEFAULT 'EN',
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_template_event_lang_channel UNIQUE (event_type, language, channel)
);

CREATE INDEX IF NOT EXISTS idx_tmpl_event_type ON notification_templates (event_type);
CREATE INDEX IF NOT EXISTS idx_tmpl_language   ON notification_templates (language);
