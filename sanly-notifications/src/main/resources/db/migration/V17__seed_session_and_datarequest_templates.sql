-- Session security & data request notification templates (EN / TK / RU)

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template)
VALUES
-- NEW_SESSION_DETECTED
('NEW_SESSION_DETECTED', 'EN', 'IN_APP',
 'New Sign-In Detected',
 'A new sign-in was detected on your SANLY account from {{deviceName}} ({{ipAddress}}). If this was not you, revoke this session immediately in your portal settings.'),
('NEW_SESSION_DETECTED', 'TK', 'IN_APP',
 'Täze giriş ýüze çykaryldy',
 'SANLY hasabyňyza {{deviceName}} ({{ipAddress}}) tarapyndan täze giriş ýüze çykaryldy. Eger bu siz däldiňiz, derrew portal sazlamalarynda bu sessiýany ýatyryn.'),
('NEW_SESSION_DETECTED', 'RU', 'IN_APP',
 'Обнаружен новый вход',
 'На ваш аккаунт SANLY выполнен вход с устройства {{deviceName}} ({{ipAddress}}). Если это были не вы — немедленно отзовите сессию в настройках портала.'),

-- SESSION_REVOKED_BY_ADMIN
('SESSION_REVOKED_BY_ADMIN', 'EN', 'IN_APP',
 'Session Revoked by Administrator',
 'One of your active sessions has been revoked by a system administrator. If this was unexpected, please contact SANLY support.'),
('SESSION_REVOKED_BY_ADMIN', 'TK', 'IN_APP',
 'Sessiýa administrator tarapyndan ýatyryldy',
 'Siziň işjeň sessiýalaryňyzyň biri ulgam dolandyryjysy tarapyndan ýatyryldy. Bu garaşylmadyk bolsa, SANLY goldaw gullugy bilen habarlaşyň.'),
('SESSION_REVOKED_BY_ADMIN', 'RU', 'IN_APP',
 'Сессия отозвана администратором',
 'Одна из ваших активных сессий была отозвана системным администратором. Если это неожиданно — обратитесь в службу поддержки SANLY.'),

-- DATA_REQUEST_RECEIVED
('DATA_REQUEST_RECEIVED', 'EN', 'IN_APP',
 'Data Request Received — {{requestCode}}',
 'Your data request {{requestCode}} ({{requestType}}) has been received. You will be notified when it is reviewed. Requests are typically processed within 30 days.'),
('DATA_REQUEST_RECEIVED', 'TK', 'IN_APP',
 'Maglumat talaby alyndy — {{requestCode}}',
 'Siziň {{requestCode}} ({{requestType}}) maglumat talabyňyz alyndy. Ol seredilende size habar berler. Talaplara adatça 30 günüň dowamynda seredilýär.'),
('DATA_REQUEST_RECEIVED', 'RU', 'IN_APP',
 'Запрос на данные получен — {{requestCode}}',
 'Ваш запрос {{requestCode}} ({{requestType}}) получен. Вы будете уведомлены о рассмотрении. Запросы обычно обрабатываются в течение 30 дней.'),

-- DATA_REQUEST_APPROVED
('DATA_REQUEST_APPROVED', 'EN', 'IN_APP',
 'Data Request Approved — {{requestCode}}',
 'Your data request {{requestCode}} has been approved. {{resolutionDescription}}'),
('DATA_REQUEST_APPROVED', 'TK', 'IN_APP',
 'Maglumat talaby tassyklandy — {{requestCode}}',
 'Siziň {{requestCode}} maglumat talabyňyz tassyklandy. {{resolutionDescription}}'),
('DATA_REQUEST_APPROVED', 'RU', 'IN_APP',
 'Запрос на данные одобрен — {{requestCode}}',
 'Ваш запрос {{requestCode}} одобрен. {{resolutionDescription}}'),

-- DATA_REQUEST_REJECTED
('DATA_REQUEST_REJECTED', 'EN', 'IN_APP',
 'Data Request Rejected — {{requestCode}}',
 'Your data request {{requestCode}} was rejected. Reason: {{reason}}. You may submit a new request with additional information.'),
('DATA_REQUEST_REJECTED', 'TK', 'IN_APP',
 'Maglumat talaby ret edildi — {{requestCode}}',
 'Siziň {{requestCode}} maglumat talabyňyz ret edildi. Sebäbi: {{reason}}. Goşmaça maglumat bilen täze talap iberip bilersiňiz.'),
('DATA_REQUEST_REJECTED', 'RU', 'IN_APP',
 'Запрос на данные отклонён — {{requestCode}}',
 'Ваш запрос {{requestCode}} отклонён. Причина: {{reason}}. Вы можете подать новый запрос с дополнительной информацией.'),

-- DATA_REQUEST_PARTIAL
('DATA_REQUEST_PARTIAL', 'EN', 'IN_APP',
 'Data Request Partially Fulfilled — {{requestCode}}',
 'Your data request {{requestCode}} has been partially fulfilled. {{resolutionDescription}}'),
('DATA_REQUEST_PARTIAL', 'TK', 'IN_APP',
 'Maglumat talaby bölekleýin ýerine ýetirildi — {{requestCode}}',
 'Siziň {{requestCode}} maglumat talabyňyz bölekleýin ýerine ýetirildi. {{resolutionDescription}}'),
('DATA_REQUEST_PARTIAL', 'RU', 'IN_APP',
 'Запрос на данные частично выполнен — {{requestCode}}',
 'Ваш запрос {{requestCode}} частично выполнен. {{resolutionDescription}}')

ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;
