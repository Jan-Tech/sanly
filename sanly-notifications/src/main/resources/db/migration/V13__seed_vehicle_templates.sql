INSERT INTO notification_templates (event_type, language, subject, body_template) VALUES
-- VEHICLE_REGISTERED
('VEHICLE_REGISTERED', 'EN', 'Vehicle Registered',
 'Your vehicle {{make}} {{model}} ({{year}}) has been registered with plate number {{plateNumber}}.'),
('VEHICLE_REGISTERED', 'TK', 'Ulag bellige alyndy',
 'Siziň ulagňyz {{make}} {{model}} ({{year}}) {{plateNumber}} belgisi bilen bellige alyndy.'),
('VEHICLE_REGISTERED', 'RU', 'Транспортное средство зарегистрировано',
 'Ваш автомобиль {{make}} {{model}} ({{year}}) зарегистрирован с номерным знаком {{plateNumber}}.'),

-- VEHICLE_TRANSFER_APPROVED
('VEHICLE_TRANSFER_APPROVED', 'EN', 'Vehicle Transfer Approved',
 'Ownership of vehicle {{plateNumber}} has been transferred successfully.'),
('VEHICLE_TRANSFER_APPROVED', 'TK', 'Ulag geçirmesi tassyklandy',
 '{{plateNumber}} belgili ulagda eýeçilik hukugy üstünlikli geçirildi.'),
('VEHICLE_TRANSFER_APPROVED', 'RU', 'Передача транспортного средства одобрена',
 'Право собственности на транспортное средство {{plateNumber}} успешно передано.'),

-- VEHICLE_TRANSFER_REJECTED
('VEHICLE_TRANSFER_REJECTED', 'EN', 'Vehicle Transfer Rejected',
 'Transfer of vehicle {{plateNumber}} was rejected. Reason: {{reason}}.'),
('VEHICLE_TRANSFER_REJECTED', 'TK', 'Ulag geçirmesi ret edildi',
 '{{plateNumber}} belgili ulagda geçirme red edildi. Sebäp: {{reason}}.'),
('VEHICLE_TRANSFER_REJECTED', 'RU', 'Передача транспортного средства отклонена',
 'Передача транспортного средства {{plateNumber}} отклонена. Причина: {{reason}}.'),

-- VEHICLE_INHERITANCE_PENDING
('VEHICLE_INHERITANCE_PENDING', 'EN', 'Vehicle Inheritance Processing Required',
 'Vehicle {{plateNumber}} previously owned by a deceased citizen requires inheritance processing.'),
('VEHICLE_INHERITANCE_PENDING', 'TK', 'Ulag mirasyna garamak zerur',
 'Aradan çykan raýata degişli {{plateNumber}} belgili ulaga miras tertibinde garamak gerekdir.'),
('VEHICLE_INHERITANCE_PENDING', 'RU', 'Требуется оформление наследования транспортного средства',
 'Транспортное средство {{plateNumber}}, принадлежавшее умершему гражданину, требует оформления наследства.'),

-- INSURANCE_EXPIRING_SOON
('INSURANCE_EXPIRING_SOON', 'EN', 'Vehicle Insurance Expiring Soon',
 'Insurance for vehicle {{plateNumber}} expires on {{expiryDate}}. Renew to avoid penalties.'),
('INSURANCE_EXPIRING_SOON', 'TK', 'Ulagyň ätiýaçlandyrmasy gutarýar',
 '{{plateNumber}} belgili ulagy üçin ätiýaçlandyrma {{expiryDate}} senesinde gutarýar. Jerimelerden gaça durmak üçin täzeläň.'),
('INSURANCE_EXPIRING_SOON', 'RU', 'Страховка транспортного средства истекает',
 'Страховка транспортного средства {{plateNumber}} истекает {{expiryDate}}. Продлите, чтобы избежать штрафов.'),

-- INSPECTION_DUE_SOON
('INSPECTION_DUE_SOON', 'EN', 'Technical Inspection Due',
 'Technical inspection for vehicle {{plateNumber}} is due by {{dueDate}}. Book an appointment.'),
('INSPECTION_DUE_SOON', 'TK', 'Tehniki barlag möhleti ýetdi',
 '{{plateNumber}} belgili ulag üçin tehniki barlag {{dueDate}} senesine çenli geçirilmeli. Wagt belläň.'),
('INSPECTION_DUE_SOON', 'RU', 'Технический осмотр должен быть пройден',
 'Технический осмотр транспортного средства {{plateNumber}} должен быть пройден до {{dueDate}}. Запишитесь.'),

-- VEHICLE_REPORTED_STOLEN
('VEHICLE_REPORTED_STOLEN', 'EN', 'Vehicle Reported Stolen',
 'Your vehicle {{plateNumber}} has been reported stolen. Report reference: {{plateNumber}}.'),
('VEHICLE_REPORTED_STOLEN', 'TK', 'Ulag ogurlandygy habar berildi',
 'Siziň {{plateNumber}} belgili ulagy ogurlandygy habar berildi. Salgylanma: {{plateNumber}}.'),
('VEHICLE_REPORTED_STOLEN', 'RU', 'Транспортное средство заявлено как угнанное',
 'Ваше транспортное средство {{plateNumber}} заявлено как угнанное. Номер заявления: {{plateNumber}}.');
