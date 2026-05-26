-- ── Court notification templates ─────────────────────────────────────────
-- 8 event types × 3 languages = 24 rows

-- COURT_CASE_FILED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('COURT_CASE_FILED', 'TK', 'IN_APP', 'Kazyýet işi açyldy',
 '#{caseNumber} belgili kazyýet işi sizi barada #{courtName} kazyýetinde açyldy. Görnüşi: #{caseType}.',
 true),
('COURT_CASE_FILED', 'RU', 'IN_APP', 'Дело открыто',
 'В суде #{courtName} возбуждено дело #{caseNumber} с вашим участием. Тип: #{caseType}.',
 true),
('COURT_CASE_FILED', 'EN', 'IN_APP', 'Court Case Filed',
 'A court case #{caseNumber} has been filed involving you at #{courtName}. Case type: #{caseType}.',
 true);

-- COURT_VERDICT_ISSUED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('COURT_VERDICT_ISSUED', 'TK', 'IN_APP', 'Karar çykaryldy',
 '#{caseNumber} belgili iş boýunça karar çykaryldy: #{verdictType}. Şikaýat möhleti: #{appealDeadline}.',
 true),
('COURT_VERDICT_ISSUED', 'RU', 'IN_APP', 'Вынесен приговор',
 'По делу #{caseNumber} вынесен вердикт: #{verdictType}. Срок подачи апелляции: #{appealDeadline}.',
 true),
('COURT_VERDICT_ISSUED', 'EN', 'IN_APP', 'Court Verdict Issued',
 'A verdict has been issued in case #{caseNumber}: #{verdictType}. Appeal deadline: #{appealDeadline}.',
 true);

-- COURT_FINE_ISSUED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('COURT_FINE_ISSUED', 'TK', 'IN_APP', 'Kazyýet jerimesi',
 '#{caseNumber} belgili iş boýunça #{amount} TMT möçberinde jerime salyndy. Möhleti: #{dueDate}. Kod: #{fineCode}.',
 true),
('COURT_FINE_ISSUED', 'RU', 'IN_APP', 'Судебный штраф',
 'По делу #{caseNumber} наложен штраф #{amount} ТМТ. Срок оплаты: #{dueDate}. Код: #{fineCode}.',
 true),
('COURT_FINE_ISSUED', 'EN', 'IN_APP', 'Court Fine Issued',
 'A fine of #{amount} TMT has been issued in case #{caseNumber}. Due: #{dueDate}. Code: #{fineCode}.',
 true);

-- FINE_OVERDUE
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('FINE_OVERDUE', 'TK', 'IN_APP', 'Jerime möhleti geçdi',
 '#{fineCode} belgili #{amount} TMT möçberindäki jerimaňyzyň möhleti geçdi. Dessine töläň.',
 true),
('FINE_OVERDUE', 'RU', 'IN_APP', 'Просроченный штраф',
 'Штраф #{fineCode} на сумму #{amount} ТМТ просрочен. Оплатите немедленно.',
 true),
('FINE_OVERDUE', 'EN', 'IN_APP', 'Fine Overdue',
 'Your fine #{fineCode} of #{amount} TMT is overdue. Pay immediately to avoid further consequences.',
 true);

-- HEARING_REMINDER
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('HEARING_REMINDER', 'TK', 'IN_APP', 'Kazyýet diňlenişigi ýatlatmasy',
 '#{caseNumber} belgili iş boýunça #{hearingDate} senesinde #{courtName} kazyýetinde diňlenişik bolup geçer.',
 true),
('HEARING_REMINDER', 'RU', 'IN_APP', 'Напоминание о заседании',
 'Напоминание: слушание по делу #{caseNumber} в суде #{courtName} состоится #{hearingDate}.',
 true),
('HEARING_REMINDER', 'EN', 'IN_APP', 'Hearing Reminder',
 'Reminder: Court hearing for case #{caseNumber} at #{courtName} on #{hearingDate}.',
 true);

-- DOCUMENT_ACCEPTED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('DOCUMENT_ACCEPTED', 'TK', 'IN_APP', 'Resminama kabul edildi',
 '#{caseNumber} belgili iş boýunça #{documentCode} belgili resminama kazyýet tarapyndan kabul edildi.',
 true),
('DOCUMENT_ACCEPTED', 'RU', 'IN_APP', 'Документ принят',
 'Документ #{documentCode} по делу #{caseNumber} принят судом.',
 true),
('DOCUMENT_ACCEPTED', 'EN', 'IN_APP', 'Document Accepted',
 'Your document #{documentCode} for case #{caseNumber} has been accepted by the court.',
 true);

-- FINE_PAYMENT_PENDING_VERIFICATION
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('FINE_PAYMENT_PENDING_VERIFICATION', 'TK', 'IN_APP', 'Töleg barlanýar',
 '#{fineCode} belgili jerime boýunça tölegiňiz alyndy we barlanýar.',
 true),
('FINE_PAYMENT_PENDING_VERIFICATION', 'RU', 'IN_APP', 'Оплата ожидает подтверждения',
 'Ваш платёж по штрафу #{fineCode} получен и ожидает подтверждения.',
 true),
('FINE_PAYMENT_PENDING_VERIFICATION', 'EN', 'IN_APP', 'Payment Pending Verification',
 'Your payment for fine #{fineCode} has been received and is pending clerk verification.',
 true);

-- FINE_PAYMENT_CONFIRMED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('FINE_PAYMENT_CONFIRMED', 'TK', 'IN_APP', 'Töleg tassyklandy',
 '#{fineCode} belgili jerime boýunça tölegiňiz tassyklandy. Ýagdaýy: TÖLENILEN.',
 true),
('FINE_PAYMENT_CONFIRMED', 'RU', 'IN_APP', 'Оплата подтверждена',
 'Ваш платёж по штрафу #{fineCode} подтверждён. Статус: ОПЛАЧЕН.',
 true),
('FINE_PAYMENT_CONFIRMED', 'EN', 'IN_APP', 'Payment Confirmed',
 'Your payment for fine #{fineCode} has been confirmed. Fine status: PAID.',
 true);
