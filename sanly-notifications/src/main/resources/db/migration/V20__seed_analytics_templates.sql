-- Analytics reporting notification templates (EN / TK / RU)

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template)
VALUES

-- DAILY_REPORT_READY
('DAILY_REPORT_READY', 'EN', 'IN_APP',
 'Daily Report Ready — {{date}}',
 'The daily government activity report for {{date}} is ready. Log into the admin portal to view platform statistics and key performance indicators.'),

('DAILY_REPORT_READY', 'TK', 'IN_APP',
 'Günlük hasabat taýyn — {{date}}',
 '{{date}} senesi üçin günlük döwlet işjeňlik hasabaty taýyn. Platforma statistikasynyyy we esasy öndürijilik görkezijilerini görmek üçin admin portalyna giriň.'),

('DAILY_REPORT_READY', 'RU', 'IN_APP',
 'Ежедневный отчёт готов — {{date}}',
 'Ежедневный отчёт о деятельности правительства за {{date}} готов. Войдите в административный портал для просмотра статистики платформы.'),

-- WEEKLY_REPORT_READY
('WEEKLY_REPORT_READY', 'EN', 'IN_APP',
 'Weekly Report Ready — Week of {{weekStart}}',
 'The weekly government report for the period {{weekStart}} to {{weekEnd}} is ready for download in the SANLY admin portal.'),

('WEEKLY_REPORT_READY', 'TK', 'IN_APP',
 'Hepdelik hasabat taýyn — {{weekStart}} hepdesinden',
 '{{weekStart}} — {{weekEnd}} döwri üçin hepdelik döwlet hasabaty SANLY admin portalynda ýüklemek üçin taýyn.'),

('WEEKLY_REPORT_READY', 'RU', 'IN_APP',
 'Еженедельный отчёт готов — неделя с {{weekStart}}',
 'Еженедельный государственный отчёт за период {{weekStart}} — {{weekEnd}} готов для скачивания в административном портале SANLY.'),

-- REPORT_GENERATED
('REPORT_GENERATED', 'EN', 'IN_APP',
 'Report Ready — {{reportType}}',
 'Your requested {{reportType}} report ({{exportCode}}) is ready for download. It expires in 24 hours. Download it from the Analytics section of the SANLY admin portal.'),

('REPORT_GENERATED', 'TK', 'IN_APP',
 'Hasabat taýyn — {{reportType}}',
 'Siziň sargyt eden {{reportType}} hasabatyňyz ({{exportCode}}) ýüklemek üçin taýyn. Ol 24 sagat soňra möhleti geçer. SANLY admin portalynyň Analitika bölüminden ýükläň.'),

('REPORT_GENERATED', 'RU', 'IN_APP',
 'Отчёт готов — {{reportType}}',
 'Запрошенный вами отчёт {{reportType}} ({{exportCode}}) готов для скачивания. Срок действия истекает через 24 часа. Скачайте его в разделе «Аналитика» административного портала SANLY.')

ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;
