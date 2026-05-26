-- ── Land Registry notification templates ─────────────────────────────────
-- 4 event types × 3 languages = 12 rows

-- PROPERTY_TRANSFER_INITIATED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('PROPERTY_TRANSFER_INITIATED', 'EN', 'IN_APP',
 'Property Transfer Initiated: {cadastralNumber}',
 'A transfer application for your property {cadastralNumber} has been submitted. Transfer type: {transferType}. An officer will review the application.',
 true),
('PROPERTY_TRANSFER_INITIATED', 'TK', 'IN_APP',
 'Emläk geçirimi başladyldy: {cadastralNumber}',
 '{cadastralNumber} emlägiňiz üçin geçirim arzasy tabşyryldy. Geçirim görnüşi: {transferType}. Işgär arzany seljerer.',
 true),
('PROPERTY_TRANSFER_INITIATED', 'RU', 'IN_APP',
 'Начата передача объекта: {cadastralNumber}',
 'Подана заявка на передачу вашей собственности {cadastralNumber}. Тип передачи: {transferType}. Заявка будет рассмотрена офицером.',
 true);

-- PROPERTY_TRANSFER_APPROVED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('PROPERTY_TRANSFER_APPROVED', 'EN', 'IN_APP',
 'Property Transfer Approved: {cadastralNumber}',
 'Ownership of property {cadastralNumber} has been successfully transferred on {transferDate}. The digital deed is now updated in the national registry.',
 true),
('PROPERTY_TRANSFER_APPROVED', 'TK', 'IN_APP',
 'Emläk geçirimi tassyklandy: {cadastralNumber}',
 '{cadastralNumber} emlägiň eýeçiligi {transferDate} senesinde üstünlikli geçirildi. Sanly resminame milli reestirde täzelendi.',
 true),
('PROPERTY_TRANSFER_APPROVED', 'RU', 'IN_APP',
 'Передача объекта одобрена: {cadastralNumber}',
 'Право собственности на объект {cadastralNumber} успешно передано {transferDate}. Электронный документ обновлён в государственном реестре.',
 true);

-- PROPERTY_TRANSFER_REJECTED
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('PROPERTY_TRANSFER_REJECTED', 'EN', 'IN_APP',
 'Property Transfer Rejected: {cadastralNumber}',
 'Your transfer application for property {cadastralNumber} was rejected. Reason: {rejectionReason}. Please contact the Land Registry for assistance.',
 true),
('PROPERTY_TRANSFER_REJECTED', 'TK', 'IN_APP',
 'Emläk geçirimi ret edildi: {cadastralNumber}',
 '{cadastralNumber} emlägi üçin geçirim arzaňyz ret edildi. Sebäp: {rejectionReason}. Kömek üçin Ýer reestiri bilen habarlaşyň.',
 true),
('PROPERTY_TRANSFER_REJECTED', 'RU', 'IN_APP',
 'Передача объекта отклонена: {cadastralNumber}',
 'Ваша заявка на передачу объекта {cadastralNumber} была отклонена. Причина: {rejectionReason}. Обратитесь в земельный реестр за помощью.',
 true);

-- PROPERTY_INHERITANCE_PENDING
INSERT INTO notification_templates (event_type, language, channel, title_template, body_template, is_active) VALUES
('PROPERTY_INHERITANCE_PENDING', 'EN', 'IN_APP',
 'Property Inheritance Pending: {cadastralNumber}',
 'Property {cadastralNumber} previously held with a deceased citizen now requires inheritance processing. Please visit the Land Registry to complete the transfer.',
 true),
('PROPERTY_INHERITANCE_PENDING', 'TK', 'IN_APP',
 'Miras garaşylýar: {cadastralNumber}',
 '{cadastralNumber} emlägi aradan çykan raýat bilen bilelikde eýeçilikde bolupdy we miras tertibini talap edýär. Geçirimi tamamlamak üçin Ýer reestrine baryň.',
 true),
('PROPERTY_INHERITANCE_PENDING', 'RU', 'IN_APP',
 'Оформление наследства: {cadastralNumber}',
 'Объект {cadastralNumber} находился в совместной собственности с умершим гражданином и требует оформления наследства. Обратитесь в земельный реестр для завершения передачи.',
 true);
