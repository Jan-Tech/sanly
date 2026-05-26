INSERT INTO notification_templates (event_type, language, subject, body_template) VALUES
-- PENSION_ACCOUNT_OPENED
('PENSION_ACCOUNT_OPENED', 'EN', 'Pension Account Opened',
 'Your pension account {{accountCode}} has been opened. Estimated retirement date: {{eligibleAt}}.'),
('PENSION_ACCOUNT_OPENED', 'TK', 'Pensiýa hasaby açyldy',
 'Siziň pensiýa hasabyňyz {{accountCode}} açyldy. Takmynan pensiýa senesi: {{eligibleAt}}.'),
('PENSION_ACCOUNT_OPENED', 'RU', 'Пенсионный счёт открыт',
 'Ваш пенсионный счёт {{accountCode}} открыт. Ориентировочная дата выхода на пенсию: {{eligibleAt}}.'),

-- CONTRIBUTION_RECEIVED
('CONTRIBUTION_RECEIVED', 'EN', 'Pension Contribution Received',
 'A pension contribution of {{amount}} TMT was received for {{contributionMonth}} from {{employerName}}.'),
('CONTRIBUTION_RECEIVED', 'TK', 'Pensiýa gatanjy alyndy',
 '{{contributionMonth}} üçin {{employerName}} tarapyndan {{amount}} TMT pensiýa gatanjy alyndy.'),
('CONTRIBUTION_RECEIVED', 'RU', 'Получен пенсионный взнос',
 'Пенсионный взнос в размере {{amount}} TMT за {{contributionMonth}} получен от {{employerName}}.'),

-- CONTRIBUTION_DUE_REMINDER
('CONTRIBUTION_DUE_REMINDER', 'EN', 'Pension Contribution Reminder',
 'Reminder: Pension contributions for {{month}} are due by the 1st. Please submit for all employees.'),
('CONTRIBUTION_DUE_REMINDER', 'TK', 'Pensiýa gatanjy hakynda ýatlatma',
 'Ýatlatma: {{month}} aýy üçin pensiýa gatanjy 1-ine çenli tabşyrylmaly. Ähli işgärler üçin tabşyryň.'),
('CONTRIBUTION_DUE_REMINDER', 'RU', 'Напоминание о пенсионных взносах',
 'Напоминание: пенсионные взносы за {{month}} должны быть поданы до 1-го числа. Подайте за всех сотрудников.'),

-- PENSION_NOW_ELIGIBLE
('PENSION_NOW_ELIGIBLE', 'EN', 'You Are Now Eligible for Your Pension',
 'You are now eligible for your pension. Apply through the SANLY portal or visit a social services office.'),
('PENSION_NOW_ELIGIBLE', 'TK', 'Siziň pensiýa hukugyňyz bar',
 'Siz indi pensiýa çykmaga hukuklysyňyz. SANLY portalynyň üsti bilen ýüz tutuň ýa-da sosial hyzmatlara baryň.'),
('PENSION_NOW_ELIGIBLE', 'RU', 'Вы имеете право на пенсию',
 'Вы теперь имеете право на пенсию. Подайте заявку через портал SANLY или обратитесь в отдел соцзащиты.'),

-- PENSION_APPROVED
('PENSION_APPROVED', 'EN', 'Retirement Application Approved',
 'Your retirement application has been approved. Monthly pension: {{amount}} TMT starting {{startDate}}.'),
('PENSION_APPROVED', 'TK', 'Pensiýa arzasy tassyklandy',
 'Siziň pensiýa arza tassyklandy. Aýlyk pensiýa: {{startDate}} senesinden başlap {{amount}} TMT.'),
('PENSION_APPROVED', 'RU', 'Заявление на пенсию одобрено',
 'Ваше заявление о выходе на пенсию одобрено. Ежемесячная пенсия: {{amount}} ТМТ с {{startDate}}.'),

-- PENSION_PAYMENT_SCHEDULED
('PENSION_PAYMENT_SCHEDULED', 'EN', 'Pension Payment Scheduled',
 'Your pension payment of {{amount}} TMT is scheduled for {{scheduledDate}}.'),
('PENSION_PAYMENT_SCHEDULED', 'TK', 'Pensiýa töleg meýilleşdirildi',
 'Siziň pensiýa tölegi {{amount}} TMT {{scheduledDate}} senesinde geçiriler.'),
('PENSION_PAYMENT_SCHEDULED', 'RU', 'Пенсионная выплата запланирована',
 'Ваша пенсионная выплата в размере {{amount}} ТМТ запланирована на {{scheduledDate}}.'),

-- PENSION_ACCOUNT_CLOSED
('PENSION_ACCOUNT_CLOSED', 'EN', 'Pension Account Closed',
 'Pension account {{accountCode}} has been closed following death registration.'),
('PENSION_ACCOUNT_CLOSED', 'TK', 'Pensiýa hasaby ýapyldy',
 'Pensiýa hasaby {{accountCode}} aradan çykmak bilen baglylykda ýapyldy.'),
('PENSION_ACCOUNT_CLOSED', 'RU', 'Пенсионный счёт закрыт',
 'Пенсионный счёт {{accountCode}} закрыт в связи с регистрацией смерти.');
