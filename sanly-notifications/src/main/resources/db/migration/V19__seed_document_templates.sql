-- Document Wallet + Status Tracker notification templates (EN / TK / RU)

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template)
VALUES

-- CERTIFICATE_GENERATED
('CERTIFICATE_GENERATED', 'EN', 'IN_APP',
 'Certificate Ready — {{certCode}}',
 'Your certified digital copy of {{docType}} is ready. Certificate code: {{certCode}}. Download it from the Documents section in the SANLY portal. This certificate is valid until {{expiresAt}}.'),

('CERTIFICATE_GENERATED', 'TK', 'IN_APP',
 'Şahadatnama taýyn — {{certCode}}',
 'Siziň {{docType}} resminamaňyzyň tassyklanan sanly nusgasy taýyn. Şahadatnama kody: {{certCode}}. Ony SANLY portalynyň Resminamalar bölüminden ýükläp bilersiňiz. Bu şahadatnama {{expiresAt}} senesine çenli güýçde bolar.'),

('CERTIFICATE_GENERATED', 'RU', 'IN_APP',
 'Сертификат готов — {{certCode}}',
 'Ваша заверенная цифровая копия {{docType}} готова. Код сертификата: {{certCode}}. Скачайте её в разделе «Документы» портала SANLY. Сертификат действителен до {{expiresAt}}.'),

-- CERTIFICATE_VERIFIED_BY_THIRD_PARTY
('CERTIFICATE_VERIFIED_BY_THIRD_PARTY', 'EN', 'IN_APP',
 'Your Certificate Was Verified',
 'Your certificate {{certCode}} ({{docType}}) was verified by a third party on {{verifiedAt}}. If this verification was unexpected, contact support through the SANLY portal.'),

('CERTIFICATE_VERIFIED_BY_THIRD_PARTY', 'TK', 'IN_APP',
 'Şahadatnamaňyz barlandy',
 'Siziň şahadatnamaňyz {{certCode}} ({{docType}}) {{verifiedAt}} senesinde üçünji tarap tarapyndan barlandy. Bu barlag garaşylmadyk bolsa, SANLY portalyndaky goldaw bölümine ýüz tutuň.'),

('CERTIFICATE_VERIFIED_BY_THIRD_PARTY', 'RU', 'IN_APP',
 'Ваш сертификат был проверен',
 'Ваш сертификат {{certCode}} ({{docType}}) был проверен третьей стороной {{verifiedAt}}. Если эта проверка неожиданна, обратитесь в поддержку через портал SANLY.'),

-- TRACKING_STATUS_UPDATED
('TRACKING_STATUS_UPDATED', 'EN', 'IN_APP',
 'Status Update — {{trackingCode}}',
 'Your application {{trackingCode}} ({{itemType}}) has a new status: {{newStatus}}. {{notes}} Track your application in the SANLY portal.'),

('TRACKING_STATUS_UPDATED', 'TK', 'IN_APP',
 'Ýagdaý täzelendi — {{trackingCode}}',
 'Siziň ýüztutmaňyz {{trackingCode}} ({{itemType}}) täze ýagdaýa geçdi: {{newStatus}}. {{notes}} Ýüztutmaňyzy SANLY portalynda yzarlaň.'),

('TRACKING_STATUS_UPDATED', 'RU', 'IN_APP',
 'Обновление статуса — {{trackingCode}}',
 'Статус вашей заявки {{trackingCode}} ({{itemType}}) обновился: {{newStatus}}. {{notes}} Отслеживайте заявку на портале SANLY.'),

-- TRACKING_ITEM_COMPLETED
('TRACKING_ITEM_COMPLETED', 'EN', 'IN_APP',
 'Application Completed — {{trackingCode}}',
 'Your application {{trackingCode}} ({{itemType}}) has been completed. {{notes}} View the full timeline in the Documents section of the SANLY portal.'),

('TRACKING_ITEM_COMPLETED', 'TK', 'IN_APP',
 'Ýüztutma tamamlandy — {{trackingCode}}',
 'Siziň ýüztutmaňyz {{trackingCode}} ({{itemType}}) tamamlandy. {{notes}} Doly çyzgyny SANLY portalynyň Resminamalar bölüminde görüň.'),

('TRACKING_ITEM_COMPLETED', 'RU', 'IN_APP',
 'Заявка завершена — {{trackingCode}}',
 'Ваша заявка {{trackingCode}} ({{itemType}}) завершена. {{notes}} Полную хронологию смотрите в разделе «Документы» портала SANLY.')

ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;
