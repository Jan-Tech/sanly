-- Appointment notification templates (EN / TK / RU)

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template)
VALUES
-- APPOINTMENT_BOOKED
('APPOINTMENT_BOOKED','EN','IN_APP',
 'Appointment Confirmed — {{appointmentCode}}',
 'Your appointment at {{officeName}} is confirmed for {{date}} at {{time}}. Service: {{serviceName}}. Code: {{appointmentCode}}. Please bring: {{requiresDocuments}}.'),
('APPOINTMENT_BOOKED','TK','IN_APP',
 'Duşuşyk tassyklandy — {{appointmentCode}}',
 '{{officeName}} edarasynda {{date}} senesinde sagat {{time}}-da duşuşygyňyz tassyklandy. Hyzmat: {{serviceName}}. Kod: {{appointmentCode}}. Getirmeli resminamalar: {{requiresDocuments}}.'),
('APPOINTMENT_BOOKED','RU','IN_APP',
 'Запись подтверждена — {{appointmentCode}}',
 'Ваша запись в {{officeName}} подтверждена на {{date}} в {{time}}. Услуга: {{serviceName}}. Код: {{appointmentCode}}. Возьмите с собой: {{requiresDocuments}}.'),

-- APPOINTMENT_REMINDER
('APPOINTMENT_REMINDER','EN','IN_APP',
 'Appointment Reminder — Tomorrow',
 'Reminder: Your appointment at {{officeName}} is tomorrow at {{time}}. Code: {{appointmentCode}}. Please arrive 5 minutes early.'),
('APPOINTMENT_REMINDER','TK','IN_APP',
 'Duşuşyk ýatlatmasy — Ertir',
 'Ýatlatma: {{officeName}} edarasynda ertir sagat {{time}}-da duşuşygyňyz bar. Kod: {{appointmentCode}}. 5 minut öň gelmek maslahat berilýär.'),
('APPOINTMENT_REMINDER','RU','IN_APP',
 'Напоминание о записи — Завтра',
 'Напоминание: ваша запись в {{officeName}} завтра в {{time}}. Код: {{appointmentCode}}. Просьба прийти на 5 минут раньше.'),

-- APPOINTMENT_CANCELLED_BY_CITIZEN
('APPOINTMENT_CANCELLED_BY_CITIZEN','EN','IN_APP',
 'Appointment Cancelled',
 'Your appointment {{appointmentCode}} at {{officeName}} on {{date}} has been cancelled. You can rebook at any time.'),
('APPOINTMENT_CANCELLED_BY_CITIZEN','TK','IN_APP',
 'Duşuşyk ýatyryldy',
 '{{officeName}} edarasynda {{date}} senesine bellenilen {{appointmentCode}} duşuşygyňyz ýatyryldy. Islendik wagyt täzeden belläp bilersiňiz.'),
('APPOINTMENT_CANCELLED_BY_CITIZEN','RU','IN_APP',
 'Запись отменена',
 'Ваша запись {{appointmentCode}} в {{officeName}} на {{date}} отменена. Вы можете записаться снова в любое время.'),

-- APPOINTMENT_CANCELLED_BY_OFFICE
('APPOINTMENT_CANCELLED_BY_OFFICE','EN','IN_APP',
 'Appointment Cancelled by Office',
 'Your appointment {{appointmentCode}} has been cancelled by the office. Reason: {{reason}}. Please rebook at your convenience.'),
('APPOINTMENT_CANCELLED_BY_OFFICE','TK','IN_APP',
 'Duşuşyk edara tarapyndan ýatyryldy',
 '{{appointmentCode}} duşuşygyňyz edara tarapyndan ýatyryldy. Sebäbi: {{reason}}. Amatly wagtyňyzda täzeden belläp bilersiňiz.'),
('APPOINTMENT_CANCELLED_BY_OFFICE','RU','IN_APP',
 'Запись отменена учреждением',
 'Ваша запись {{appointmentCode}} отменена учреждением. Причина: {{reason}}. Пожалуйста, запишитесь снова в удобное время.'),

-- APPOINTMENT_COMPLETED
('APPOINTMENT_COMPLETED','EN','IN_APP',
 'Appointment Completed',
 'Your appointment at {{officeName}} is complete. Thank you for using SANLY! Please rate your experience in the portal.'),
('APPOINTMENT_COMPLETED','TK','IN_APP',
 'Duşuşyk tamamlandy',
 '{{officeName}} edarasyndaky duşuşygyňyz tamamlandy. SANLY hyzmatyny saýlandygyňyz üçin sag boluň! Portaldaky tejribäňizi bahalandyrmagy haýyş edýäris.'),
('APPOINTMENT_COMPLETED','RU','IN_APP',
 'Запись завершена',
 'Ваша запись в {{officeName}} завершена. Спасибо за использование SANLY! Оцените качество обслуживания на портале.'),

-- WAITLIST_SLOT_AVAILABLE
('WAITLIST_SLOT_AVAILABLE','EN','IN_APP',
 'Slot Available — Act Now!',
 'Good news! A slot has opened at {{officeName}} for {{serviceName}} on {{date}} at {{time}}. Book within 2 hours to secure it.'),
('WAITLIST_SLOT_AVAILABLE','TK','IN_APP',
 'Ýer açyldy — Dessine bron ediň!',
 'Hoş habar! {{officeName}} edarasynda {{serviceName}} hyzmat üçin {{date}} senesinde sagat {{time}}-da ýer açyldy. Ony kepillendirmek üçin 2 sagatyň içinde bron ediň.'),
('WAITLIST_SLOT_AVAILABLE','RU','IN_APP',
 'Место доступно — Действуйте сейчас!',
 'Хорошая новость! В {{officeName}} открылось место для услуги {{serviceName}} на {{date}} в {{time}}. Запишитесь в течение 2 часов, чтобы занять его.'),

-- WAITLIST_EXPIRED
('WAITLIST_EXPIRED','EN','IN_APP',
 'Waitlist Entry Expired',
 'Your waitlist entry for {{officeName}} has expired after 30 days. Please re-register if you still need an appointment.'),
('WAITLIST_EXPIRED','TK','IN_APP',
 'Nobat ýazgysy möhleti geçdi',
 '{{officeName}} edarasy üçin nobat ýazgyňyzyň möhleti 30 günden soň geçdi. Duşuşyk gerek bolsa, täzeden ýazylmagyňyzy haýyş edýäris.'),
('WAITLIST_EXPIRED','RU','IN_APP',
 'Запись в очередь истекла',
 'Ваша запись в очередь для {{officeName}} истекла после 30 дней ожидания. Пожалуйста, зарегистрируйтесь снова, если вам всё ещё нужна запись.')

ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;
