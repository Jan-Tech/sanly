-- ── Customs notification templates ────────────────────────────────────────
-- 5 event types × 3 languages = 15 rows

-- CUSTOMS_DECLARATION_SUBMITTED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('CUSTOMS_DECLARATION_SUBMITTED', 'TK', 'IN_APP', 'Gümrük beýannamasy tabşyryldy',
 '#{declarationCode} belgili gümrük beýannamañyz #{portName} nokadynda tabşyryldy. Töleg: #{dutiesOwed} TMT.',
 true),
('CUSTOMS_DECLARATION_SUBMITTED', 'RU', 'IN_APP', 'Таможенная декларация подана',
 'Ваша декларация #{declarationCode} подана на пункте #{portName}. К оплате: #{dutiesOwed} ТМТ.',
 true),
('CUSTOMS_DECLARATION_SUBMITTED', 'EN', 'IN_APP', 'Customs Declaration Submitted',
 'Your customs declaration #{declarationCode} has been submitted at #{portName}. Duties owed: #{dutiesOwed} TMT.',
 true);

-- CUSTOMS_DECLARATION_CLEARED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('CUSTOMS_DECLARATION_CLEARED', 'TK', 'IN_APP', 'Gümrük rugsady berildi',
 '#{declarationCode} belgili beýannamaňyz tassyklandy. Ýüküňiz geçmäge rugsat berildi.',
 true),
('CUSTOMS_DECLARATION_CLEARED', 'RU', 'IN_APP', 'Таможенное разрешение получено',
 'Декларация #{declarationCode} одобрена. Ваш груз разрешён к прохождению.',
 true),
('CUSTOMS_DECLARATION_CLEARED', 'EN', 'IN_APP', 'Customs Declaration Cleared',
 'Your declaration #{declarationCode} has been cleared. Your shipment is authorized to proceed.',
 true);

-- CUSTOMS_DECLARATION_REJECTED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('CUSTOMS_DECLARATION_REJECTED', 'TK', 'IN_APP', 'Gümrük beýannamasy ret edildi',
 '#{declarationCode} belgili beýannamaňyz ret edildi. Sebäp: #{reason}',
 true),
('CUSTOMS_DECLARATION_REJECTED', 'RU', 'IN_APP', 'Таможенная декларация отклонена',
 'Декларация #{declarationCode} была отклонена. Причина: #{reason}',
 true),
('CUSTOMS_DECLARATION_REJECTED', 'EN', 'IN_APP', 'Customs Declaration Rejected',
 'Your declaration #{declarationCode} was rejected. Reason: #{reason}.',
 true);

-- CUSTOMS_DECLARATION_HELD
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('CUSTOMS_DECLARATION_HELD', 'TK', 'IN_APP', 'Beýannama barlag astynda',
 '#{declarationCode} belgili beýannamaňyz #{portName} nokadynda barlag üçin saklandy. Netije barada habar beriler.',
 true),
('CUSTOMS_DECLARATION_HELD', 'RU', 'IN_APP', 'Декларация на проверке',
 'Декларация #{declarationCode} задержана для досмотра на пункте #{portName}. Вы будете уведомлены о результате.',
 true),
('CUSTOMS_DECLARATION_HELD', 'EN', 'IN_APP', 'Declaration Held for Inspection',
 'Your declaration #{declarationCode} has been placed on hold for inspection at #{portName}. You will be notified of the result.',
 true);

-- CUSTOMS_DUTIES_REMINDER
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('CUSTOMS_DUTIES_REMINDER', 'TK', 'IN_APP', 'Gümrük tölegini ýatlatma',
 '#{declarationCode} belgili beýannama boýunça #{amount} TMT gümrük tölegi garaşýar. Rugsat almak üçin töläň.',
 true),
('CUSTOMS_DUTIES_REMINDER', 'RU', 'IN_APP', 'Напоминание об уплате таможенных пошлин',
 'По декларации #{declarationCode} ожидается уплата таможенных пошлин: #{amount} ТМТ. Оплатите для получения разрешения.',
 true),
('CUSTOMS_DUTIES_REMINDER', 'EN', 'IN_APP', 'Customs Duties Reminder',
 'Outstanding duties of #{amount} TMT are owed on declaration #{declarationCode}. Please settle to proceed with clearance.',
 true);
