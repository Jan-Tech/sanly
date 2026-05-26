-- ── Education notification templates ──────────────────────────────────────
-- 4 event types × 3 languages = 12 rows

-- DIPLOMA_ISSUED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('DIPLOMA_ISSUED', 'EN', 'IN_APP',
 'Diploma Issued: {diplomaCode}',
 'Congratulations! Your diploma {diplomaCode} from {institutionName} for {programName} has been officially issued and is now verifiable online.',
 true),
('DIPLOMA_ISSUED', 'TK', 'IN_APP',
 'Diplom çykaryldy: {diplomaCode}',
 'Gutlaýarys! {institutionName} tarapyndan {programName} üçin {diplomaCode} diplomy resmi taýdan çykaryldy we indi onlaýn barlanýar.',
 true),
('DIPLOMA_ISSUED', 'RU', 'IN_APP',
 'Диплом выдан: {diplomaCode}',
 'Поздравляем! Ваш диплом {diplomaCode} от {institutionName} по программе {programName} официально выдан и доступен для проверки онлайн.',
 true);

-- DIPLOMA_REVOKED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('DIPLOMA_REVOKED', 'EN', 'IN_APP',
 'Diploma Revoked: {diplomaCode}',
 'Your diploma {diplomaCode} has been revoked. Reason: {reason}. Please contact {institutionName} for further information.',
 true),
('DIPLOMA_REVOKED', 'TK', 'IN_APP',
 'Diplom ýatyryldy: {diplomaCode}',
 '{diplomaCode} diplomyňyz ýatyryldy. Sebäp: {reason}. Giňişleýin maglumat üçin {institutionName} bilen habarlaşyň.',
 true),
('DIPLOMA_REVOKED', 'RU', 'IN_APP',
 'Диплом аннулирован: {diplomaCode}',
 'Ваш диплом {diplomaCode} был аннулирован. Причина: {reason}. Обратитесь в {institutionName} для получения подробной информации.',
 true);

-- ENROLLMENT_CONFIRMED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('ENROLLMENT_CONFIRMED', 'EN', 'IN_APP',
 'Enrollment Confirmed at {institutionName}',
 'You have been successfully enrolled at {institutionName} in the {programName} program starting {enrollmentDate}.',
 true),
('ENROLLMENT_CONFIRMED', 'TK', 'IN_APP',
 '{institutionName} okuwyna kabul edildiňiz',
 'Siz {enrollmentDate} senesinden başlap {institutionName} okuw mekdebiniň {programName} ugry boýunça okamaga kabul edildiňiz.',
 true),
('ENROLLMENT_CONFIRMED', 'RU', 'IN_APP',
 'Зачисление подтверждено: {institutionName}',
 'Вы успешно зачислены в {institutionName} на программу {programName} с {enrollmentDate}.',
 true);

-- SCHOOL_ENROLLMENT_QUEUED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('SCHOOL_ENROLLMENT_QUEUED', 'EN', 'IN_APP',
 'School Enrollment Queue: {childName}',
 'Your child {childName} has been added to the school enrollment queue for the {expectedSchoolYear} academic year. An education officer will contact you shortly.',
 true),
('SCHOOL_ENROLLMENT_QUEUED', 'TK', 'IN_APP',
 'Mekdep üçin nobata alyndy: {childName}',
 '{childName} atly çagaňyz {expectedSchoolYear}-nji okuw ýyly üçin mekdep nobatyna goşuldy. Bilim işgäri siziň bilen habarlaşar.',
 true),
('SCHOOL_ENROLLMENT_QUEUED', 'RU', 'IN_APP',
 'Очередь на зачисление: {childName}',
 'Ваш ребёнок {childName} добавлен в очередь на зачисление в школу на {expectedSchoolYear} учебный год. С вами свяжется сотрудник отдела образования.',
 true);
