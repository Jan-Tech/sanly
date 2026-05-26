-- Seed notification templates: 3 languages × required event types
-- Template variables use {variable} syntax — replaced at send time.
-- ON CONFLICT DO NOTHING makes this migration re-runnable.

-- ─── DATA_ACCESSED_BY_POLICE ───────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_POLICE', 'EN', 'ALL',
 'Your data was accessed by Police Authority',
 'On {accessedAt}, the Police Authority accessed your {dataType} record. Purpose: {purposeCode}. Case reference: {caseReference}. If you believe this access was unauthorized, contact the Data Protection Authority.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_POLICE', 'RU', 'ALL',
 'Полиция получила доступ к вашим данным',
 '{accessedAt} Полиция получила доступ к вашей записи {dataType}. Цель: {purposeCode}. Номер дела: {caseReference}. Если вы считаете этот доступ несанкционированным, обратитесь в Орган по защите данных.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_POLICE', 'TK', 'ALL',
 'Siziň maglumatlaryňyza polisiýa girdi',
 '{accessedAt} senesinde Polisiýa siziň {dataType} ýazgyňyza girdi. Maksat: {purposeCode}. Iş belgisi: {caseReference}. Bu giriş rugsatsyz bolsa, Maglumat Goragy Edarasyna ýüz tutuň.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── DATA_ACCESSED_BY_TAX ─────────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_TAX', 'EN', 'ALL',
 'Your data was accessed by Tax Authority',
 'On {accessedAt}, the Tax Authority accessed your {dataType} record. Purpose: {purposeCode}. If you have questions, contact the State Tax Service.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_TAX', 'RU', 'ALL',
 'Налоговая служба получила доступ к вашим данным',
 '{accessedAt} Налоговая служба получила доступ к вашей записи {dataType}. Цель: {purposeCode}. По вопросам обращайтесь в Государственную налоговую службу.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_TAX', 'TK', 'ALL',
 'Siziň maglumatlaryňyza Salgyt gullugy girdi',
 '{accessedAt} senesinde Salgyt gullugy siziň {dataType} ýazgyňyza girdi. Maksat: {purposeCode}. Soraglar üçin Döwlet Salgyt gullugyna ýüz tutuň.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── DATA_ACCESSED_BY_DMV ─────────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_DMV', 'EN', 'ALL',
 'Your data was accessed by DMV',
 'On {accessedAt}, the Department of Motor Vehicles accessed your {dataType} record. Purpose: {purposeCode}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_DMV', 'RU', 'ALL',
 'ГАИ получила доступ к вашим данным',
 '{accessedAt} Государственная автомобильная инспекция получила доступ к вашей записи {dataType}. Цель: {purposeCode}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_DMV', 'TK', 'ALL',
 'Siziň maglumatlaryňyza ÝHI girdi',
 '{accessedAt} senesinde Ýol Hereket Inspeksiýasy siziň {dataType} ýazgyňyza girdi. Maksat: {purposeCode}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── DATA_ACCESSED_BY_MEDICAL ─────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_MEDICAL', 'EN', 'ALL',
 'Your medical data was accessed',
 'On {accessedAt}, a medical institution accessed your {dataType} record. Purpose: {purposeCode}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_MEDICAL', 'RU', 'ALL',
 'Медицинское учреждение получило доступ к вашим данным',
 '{accessedAt} Медицинское учреждение получило доступ к вашей записи {dataType}. Цель: {purposeCode}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_MEDICAL', 'TK', 'ALL',
 'Lukmançylyk edarasy siziň maglumatlaryňyza girdi',
 '{accessedAt} senesinde Lukmançylyk edarasy siziň {dataType} ýazgyňyza girdi. Maksat: {purposeCode}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── DATA_ACCESSED_BY_BUSINESS ────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_BUSINESS', 'EN', 'ALL',
 'Your data was accessed by Business Registry',
 'On {accessedAt}, the Business Registry accessed your {dataType} record. Purpose: {purposeCode}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_BUSINESS', 'RU', 'ALL',
 'Реестр предприятий получил доступ к вашим данным',
 '{accessedAt} Реестр предприятий получил доступ к вашей записи {dataType}. Цель: {purposeCode}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DATA_ACCESSED_BY_BUSINESS', 'TK', 'ALL',
 'Işewürlik Reestri siziň maglumatlaryňyza girdi',
 '{accessedAt} senesinde Işewürlik Reestri siziň {dataType} ýazgyňyza girdi. Maksat: {purposeCode}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── DRIVING_LICENSE_ISSUED ───────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DRIVING_LICENSE_ISSUED', 'EN', 'ALL',
 'Your driving license has been issued',
 'Your driving license {licenseNumber} (Category {category}) was issued on {issuedAt} and is valid until {expiresAt}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DRIVING_LICENSE_ISSUED', 'RU', 'ALL',
 'Ваше водительское удостоверение выдано',
 'Водительское удостоверение {licenseNumber} (категория {category}) выдано {issuedAt} и действительно до {expiresAt}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DRIVING_LICENSE_ISSUED', 'TK', 'ALL',
 'Siziň sürüjilik şahadatnamaňyz berildi',
 'Sürüjilik şahadatnamaňyz {licenseNumber} (Kategoriýa {category}) {issuedAt} senesinde berildi we {expiresAt} senesine çenli hereket edýär.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── DRIVING_LICENSE_SUSPENDED ────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DRIVING_LICENSE_SUSPENDED', 'EN', 'ALL',
 'Your driving license has been suspended',
 'Your driving license {licenseNumber} has been suspended as of {suspendedAt}. Reason: {reason}. Contact the DMV for further information.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DRIVING_LICENSE_SUSPENDED', 'RU', 'ALL',
 'Ваше водительское удостоверение приостановлено',
 'Водительское удостоверение {licenseNumber} приостановлено с {suspendedAt}. Причина: {reason}. Обратитесь в ГАИ для получения дополнительной информации.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DRIVING_LICENSE_SUSPENDED', 'TK', 'ALL',
 'Siziň sürüjilik şahadatnamaňyz togtadyldy',
 'Sürüjilik şahadatnamaňyz {licenseNumber} {suspendedAt} senesinden başlap togtadyldy. Sebäbi: {reason}. Goşmaça maglumat üçin ÝHI-a ýüz tutuň.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── CRIMINAL_RECORD_ADDED ────────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CRIMINAL_RECORD_ADDED', 'EN', 'ALL',
 'A criminal record has been added to your profile',
 'A {offenseType} record was added to your profile on {recordedAt} by officer {officerBadge}. If you believe this is an error, you may appeal through the SANLY citizen portal.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CRIMINAL_RECORD_ADDED', 'RU', 'ALL',
 'В вашем профиле добавлена судимость',
 'Запись о правонарушении {offenseType} была добавлена в ваш профиль {recordedAt} офицером {officerBadge}. Если вы считаете это ошибкой, вы можете подать апелляцию через гражданский портал SANLY.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CRIMINAL_RECORD_ADDED', 'TK', 'ALL',
 'Profilyňyza jenaýat ýazgysy goşuldy',
 '{recordedAt} senesinde ofiser {officerBadge} tarapyndan profilyňyza {offenseType} jenaýat ýazgysy goşuldy. Bu ýalňyşlyk diýip hasap edýän bolsaňyz, SANLY raýat portaly arkaly şikaýat edip bilersiňiz.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── TAX_STATUS_CHANGED ───────────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('TAX_STATUS_CHANGED', 'EN', 'ALL',
 'Your tax status has changed',
 'Your tax compliance status has been updated to {newStatus} as of {changedAt}. Tax ID: {taxId}. Log in to the SANLY portal for details.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('TAX_STATUS_CHANGED', 'RU', 'ALL',
 'Ваш налоговый статус изменён',
 'Ваш налоговый статус обновлён до {newStatus} с {changedAt}. ИНН: {taxId}. Войдите на портал SANLY для получения подробностей.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('TAX_STATUS_CHANGED', 'TK', 'ALL',
 'Siziň salgyt ýagdaýyňyz üýtgedi',
 'Siziň salgyt laýyklygy ýagdaýyňyz {changedAt} senesinde {newStatus} diýip täzelendi. Salgyt belgisi: {taxId}. Jikme-jikler üçin SANLY portala giriň.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── BUSINESS_REGISTRATION_APPROVED ──────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('BUSINESS_REGISTRATION_APPROVED', 'EN', 'ALL',
 'Your business registration has been approved',
 'Your business registration application for {businessName} has been approved. Registration number: {registrationNumber}. Effective date: {approvedAt}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('BUSINESS_REGISTRATION_APPROVED', 'RU', 'ALL',
 'Ваша заявка на регистрацию бизнеса одобрена',
 'Заявка на регистрацию бизнеса {businessName} одобрена. Регистрационный номер: {registrationNumber}. Дата вступления в силу: {approvedAt}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('BUSINESS_REGISTRATION_APPROVED', 'TK', 'ALL',
 'Işewürligiňizi hasaba almak tassyklandy',
 '{businessName} işewürligini hasaba almak üçin ýüz tutmaňyz tassyklandy. Hasaba alyş belgisi: {registrationNumber}. Güýje giriş senesi: {approvedAt}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── BUSINESS_REGISTRATION_REJECTED ──────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('BUSINESS_REGISTRATION_REJECTED', 'EN', 'ALL',
 'Your business registration has been rejected',
 'Your business registration application for {businessName} has been rejected. Reason: {rejectionReason}. You may reapply after addressing the stated reason.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('BUSINESS_REGISTRATION_REJECTED', 'RU', 'ALL',
 'Ваша заявка на регистрацию бизнеса отклонена',
 'Заявка на регистрацию бизнеса {businessName} отклонена. Причина: {rejectionReason}. Вы можете повторно подать заявку после устранения указанной причины.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('BUSINESS_REGISTRATION_REJECTED', 'TK', 'ALL',
 'Işewürligiňizi hasaba almak ret edildi',
 '{businessName} işewürligini hasaba almak üçin ýüz tutmaňyz ret edildi. Sebäbi: {rejectionReason}. Görkezilen sebäbi düzedenden soň ýene-de ýüz tutup bilersiňiz.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── CIVIL_BIRTH_REGISTERED ───────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CIVIL_BIRTH_REGISTERED', 'EN', 'ALL',
 'Birth registered: {childFullName}',
 'A birth record has been registered for {childFullName} on {dateOfBirth}. Certificate number: {certificateNumber}. Registered by officer: {officerName}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CIVIL_BIRTH_REGISTERED', 'RU', 'ALL',
 'Рождение зарегистрировано: {childFullName}',
 'Запись о рождении {childFullName} зарегистрирована {dateOfBirth}. Номер свидетельства: {certificateNumber}. Зарегистрировал офицер: {officerName}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CIVIL_BIRTH_REGISTERED', 'TK', 'ALL',
 'Dogluş hasaba alyndy: {childFullName}',
 '{childFullName} üçin dogluş ýazgysy {dateOfBirth} senesinde hasaba alyndy. Şahadatnama belgisi: {certificateNumber}. Ofiser: {officerName}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── CIVIL_MARRIAGE_REGISTERED ────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CIVIL_MARRIAGE_REGISTERED', 'EN', 'ALL',
 'Marriage registered',
 'Your marriage has been registered on {marriageDate}. Certificate number: {certificateNumber}. Spouse NIN: {spouseNin}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CIVIL_MARRIAGE_REGISTERED', 'RU', 'ALL',
 'Брак зарегистрирован',
 'Ваш брак зарегистрирован {marriageDate}. Номер свидетельства: {certificateNumber}. ИНН супруга/супруги: {spouseNin}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CIVIL_MARRIAGE_REGISTERED', 'TK', 'ALL',
 'Nikah hasaba alyndy',
 'Siziň nikaňyz {marriageDate} senesinde hasaba alyndy. Şahadatnama belgisi: {certificateNumber}. Jüpüňiziň şahsy belgisi: {spouseNin}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── CIVIL_DEATH_REGISTERED ───────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CIVIL_DEATH_REGISTERED', 'EN', 'ALL',
 'Death record registered',
 'A death record has been registered for {deceasedFullName} on {dateOfDeath}. Certificate number: {certificateNumber}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CIVIL_DEATH_REGISTERED', 'RU', 'ALL',
 'Запись о смерти зарегистрирована',
 'Запись о смерти {deceasedFullName} зарегистрирована {dateOfDeath}. Номер свидетельства: {certificateNumber}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CIVIL_DEATH_REGISTERED', 'TK', 'ALL',
 'Ölüm ýazgysy hasaba alyndy',
 '{deceasedFullName} üçin ölüm ýazgysy {dateOfDeath} senesinde hasaba alyndy. Şahadatnama belgisi: {certificateNumber}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── ANOMALY_DETECTED_ON_YOUR_DATA ────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('ANOMALY_DETECTED_ON_YOUR_DATA', 'EN', 'ALL',
 'Unusual access pattern detected on your data',
 'Our system detected an unusual access pattern involving your records by {institutionName} on {detectedAt}. This has been flagged for review by the Data Protection Authority. No action is required from you.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('ANOMALY_DETECTED_ON_YOUR_DATA', 'RU', 'ALL',
 'Обнаружен необычный шаблон доступа к вашим данным',
 'Наша система обнаружила необычный шаблон доступа к вашим записям со стороны {institutionName} {detectedAt}. Это зафиксировано для проверки органом по защите данных. Никаких действий с вашей стороны не требуется.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('ANOMALY_DETECTED_ON_YOUR_DATA', 'TK', 'ALL',
 'Maglumatlaryňyza adaty bolmadyk giriş anyklandy',
 'Ulgamymyz {detectedAt} senesinde {institutionName} tarapyndan siziň ýazgylaryňyza adaty bolmadyk giriş ýüze çykardy. Bu Maglumat Goragy Edarasy tarapyndan seredilmek üçin bellendi. Siziň tarapyňyzdan hiç hili hereket talap edilmeýär.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── MEDICAL_RECORD_ADDED ─────────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('MEDICAL_RECORD_ADDED', 'EN', 'ALL',
 'A medical record has been added',
 'A {testType} medical record was added to your profile on {recordedAt} at {clinicName}. Result: {result}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('MEDICAL_RECORD_ADDED', 'RU', 'ALL',
 'Добавлена медицинская запись',
 'Медицинская запись типа {testType} добавлена в ваш профиль {recordedAt} в {clinicName}. Результат: {result}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('MEDICAL_RECORD_ADDED', 'TK', 'ALL',
 'Lukmançylyk ýazgysy goşuldy',
 '{recordedAt} senesinde {clinicName} klinikasyna siziň profilyňyza {testType} görnüşli lukmançylyk ýazgysy goşuldy. Netije: {result}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── CITIZEN_STATUS_CHANGED ───────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CITIZEN_STATUS_CHANGED', 'EN', 'ALL',
 'Your citizen registry status has changed',
 'Your status in the Citizen Registry has been updated to {newStatus} as of {changedAt}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CITIZEN_STATUS_CHANGED', 'RU', 'ALL',
 'Ваш статус в реестре граждан изменён',
 'Ваш статус в реестре граждан обновлён до {newStatus} с {changedAt}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CITIZEN_STATUS_CHANGED', 'TK', 'ALL',
 'Raýatlar reýestrindäki ýagdaýyňyz üýtgedi',
 'Siziň Raýatlar Reýestrindäki ýagdaýyňyz {changedAt} senesinde {newStatus} diýip täzelendi.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;
