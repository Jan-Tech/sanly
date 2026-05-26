-- OTP_LOGIN templates (SMS-only, 3 languages)
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template)
VALUES
    ('OTP_LOGIN', 'EN', 'SMS',
     'SANLY Login OTP',
     'Your SANLY login code is: {{otpCode}}. Valid for 5 minutes. Do not share this code.'),

    ('OTP_LOGIN', 'TK', 'SMS',
     'SANLY Giriş kody',
     'SANLY giriş koduňyz: {{otpCode}}. 5 minutlyk geçerlidir. Bu kody paýlaşmaň.'),

    ('OTP_LOGIN', 'RU', 'SMS',
     'Код входа SANLY',
     'Ваш код входа SANLY: {{otpCode}}. Действителен 5 минут. Не сообщайте этот код никому.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;
