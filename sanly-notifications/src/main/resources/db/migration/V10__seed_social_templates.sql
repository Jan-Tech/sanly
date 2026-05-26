-- ── Social Benefits notification templates ────────────────────────────────
-- 7 event types × 3 languages = 21 rows

-- BENEFIT_CLAIM_APPROVED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('BENEFIT_CLAIM_APPROVED', 'TK', 'IN_APP', 'Ýeňillik tassyklanyldy', 'Siziň ýeňillik #{claimCode} boýunça arzaňyz tassyklanyldy.', true),
('BENEFIT_CLAIM_APPROVED', 'RU', 'IN_APP', 'Льгота одобрена', 'Ваше заявление на льготу #{claimCode} одобрено.', true),
('BENEFIT_CLAIM_APPROVED', 'EN', 'IN_APP', 'Benefit Claim Approved', 'Your benefit claim #{claimCode} has been approved.', true);

-- BENEFIT_CLAIM_REJECTED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('BENEFIT_CLAIM_REJECTED', 'TK', 'IN_APP', 'Ýeňillik ret edildi', 'Siziň ýeňillik #{claimCode} boýunça arzaňyz ret edildi. Sebäp: #{reason}', true),
('BENEFIT_CLAIM_REJECTED', 'RU', 'IN_APP', 'Льгота отклонена', 'Ваше заявление на льготу #{claimCode} отклонено. Причина: #{reason}', true),
('BENEFIT_CLAIM_REJECTED', 'EN', 'IN_APP', 'Benefit Claim Rejected', 'Your benefit claim #{claimCode} has been rejected. Reason: #{reason}', true);

-- BENEFIT_AUTO_TRIGGERED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('BENEFIT_AUTO_TRIGGERED', 'TK', 'IN_APP', 'Ýeňillik awtomatik işjeňleşdirildi', 'Siziň #{programCode} programmasy boýunça ýeňiligiňiz awtomatik işjeňleşdirildi.', true),
('BENEFIT_AUTO_TRIGGERED', 'RU', 'IN_APP', 'Льгота автоматически активирована', 'Ваша льгота по программе #{programCode} была автоматически активирована.', true),
('BENEFIT_AUTO_TRIGGERED', 'EN', 'IN_APP', 'Benefit Auto-Triggered', 'Your benefit under program #{programCode} has been automatically activated.', true);

-- BENEFIT_PAYMENT_SCHEDULED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('BENEFIT_PAYMENT_SCHEDULED', 'TK', 'IN_APP', 'Töleg meýilleşdirildi', '#{period} döwri üçin #{amount} töleg meýilleşdirildi.', true),
('BENEFIT_PAYMENT_SCHEDULED', 'RU', 'IN_APP', 'Платёж запланирован', 'Платёж #{amount} запланирован за период #{period}.', true),
('BENEFIT_PAYMENT_SCHEDULED', 'EN', 'IN_APP', 'Payment Scheduled', 'A payment of #{amount} has been scheduled for period #{period}.', true);

-- PENSION_ELIGIBLE
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('PENSION_ELIGIBLE', 'TK', 'IN_APP', 'Pensiýa hukugy açyldy', 'Siz #{eligibleAt} senesinden başlap pensiýa almaga hukuklydyňyz.', true),
('PENSION_ELIGIBLE', 'RU', 'IN_APP', 'Право на пенсию открыто', 'Вы стали правомочны для получения пенсии с #{eligibleAt}.', true),
('PENSION_ELIGIBLE', 'EN', 'IN_APP', 'Pension Eligibility Reached', 'You are now eligible for pension as of #{eligibleAt}.', true);

-- UNEMPLOYMENT_BENEFIT_EXPIRED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('UNEMPLOYMENT_BENEFIT_EXPIRED', 'TK', 'IN_APP', 'Işsizlik ýeňiligi gutardy', 'Siziň işsizlik ýeňillik döwrüňiz gutardy.', true),
('UNEMPLOYMENT_BENEFIT_EXPIRED', 'RU', 'IN_APP', 'Пособие по безработице истекло', 'Ваш период получения пособия по безработице истёк.', true),
('UNEMPLOYMENT_BENEFIT_EXPIRED', 'EN', 'IN_APP', 'Unemployment Benefit Expired', 'Your unemployment benefit period has expired.', true);

-- MARRIAGE_BENEFIT_INFO
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('MARRIAGE_BENEFIT_INFO', 'TK', 'IN_APP', 'Nikah ýeňilikleri barada maglumat', 'Nikah baglanyşygy bilen bagly ýeňillikler we goldaw programmalary barada has giňişleýin maglumat almak üçin Sosial hyzmat merkezine ýüz tutuň.', true),
('MARRIAGE_BENEFIT_INFO', 'RU', 'IN_APP', 'Информация о льготах при вступлении в брак', 'В связи с регистрацией брака вы можете иметь право на ряд льгот. Обратитесь в Центр социального обслуживания.', true),
('MARRIAGE_BENEFIT_INFO', 'EN', 'IN_APP', 'Marriage Benefit Information', 'You may be eligible for social benefits related to your recent marriage registration. Contact the Social Service Center for details.', true);
