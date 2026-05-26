-- Life Event Automation notification templates (EN / RU / TK)

-- ─── CHILD_BENEFIT_TRIGGERED ──────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CHILD_BENEFIT_TRIGGERED','EN','ALL',
 'Child benefit registered automatically',
 'A child benefit has been automatically registered for your child {childFullName} (born {birthDate}). '
 || 'No application is needed — the benefit will be processed by the Tax Service. '
 || 'Reference: {childNationalId}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CHILD_BENEFIT_TRIGGERED','RU','ALL',
 'Детское пособие оформлено автоматически',
 'Детское пособие автоматически зарегистрировано для вашего ребёнка {childFullName} (родился {birthDate}). '
 || 'Заявление не требуется — пособие будет обработано Налоговой службой. '
 || 'Идентификатор: {childNationalId}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('CHILD_BENEFIT_TRIGGERED','TK','ALL',
 'Çaga ýeňilligi awtomatik hasaba alyndy',
 'Çagaňyz {childFullName} ({birthDate} senesinde doglan) üçin çaga ýeňilligi awtomatik hasaba alyndy. '
 || 'Ýüz tutma talap edilmeýär — ýeňillik Salgyt gullugy tarapyndan işlener. '
 || 'Belgi: {childNationalId}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── SCHOOL_ENROLLMENT_REMINDER ───────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('SCHOOL_ENROLLMENT_REMINDER','EN','ALL',
 'School enrollment reminder for {childFullName}',
 'Your child {childFullName} is turning 6 this year and is due to start school in September {schoolYear}. '
 || 'Please contact your local school district to complete enrollment. '
 || 'Child NIN: {childNationalId}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('SCHOOL_ENROLLMENT_REMINDER','RU','ALL',
 'Напоминание о зачислении в школу: {childFullName}',
 'Вашему ребёнку {childFullName} в этом году исполняется 6 лет — в сентябре {schoolYear} ему/ей пора идти в школу. '
 || 'Обратитесь в местный школьный округ для оформления записи. '
 || 'ИНН ребёнка: {childNationalId}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('SCHOOL_ENROLLMENT_REMINDER','TK','ALL',
 '{childFullName} üçin mekdebe ýazylmak baradaky ýatlatma',
 'Çagaňyz {childFullName} bu ýyl 6 ýaşa dolýar we {schoolYear} ýylynda sentýabrda mekdebe gitmeli. '
 || 'Ýazylmak üçin ýerli mekdep bölümine ýüz tutuň. '
 || 'Çaganyň şahsy belgisi: {childNationalId}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── VACCINATION_DUE_SOON ─────────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('VACCINATION_DUE_SOON','EN','ALL',
 'Vaccination due soon: {vaccineName}',
 'A vaccination appointment for {childFullName} is due on {dueDate}. '
 || 'Vaccine: {vaccineName}. Please visit your local health clinic or appointment centre. '
 || 'Child NIN: {childNationalId}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('VACCINATION_DUE_SOON','RU','ALL',
 'Скоро вакцинация: {vaccineName}',
 'Дата вакцинации {childFullName} наступает {dueDate}. '
 || 'Вакцина: {vaccineName}. Обратитесь в ближайшую поликлинику или центр записи. '
 || 'ИНН ребёнка: {childNationalId}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('VACCINATION_DUE_SOON','TK','ALL',
 'Sanjym möhleti ýakynlaşýar: {vaccineName}',
 '{childFullName} üçin {dueDate} senesinde sanjym möhleti gelýär. '
 || 'Sanjym: {vaccineName}. Ýerli saglyk merkezine ýüz tutuň. '
 || 'Çaganyň şahsy belgisi: {childNationalId}.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── BENEFITS_CANCELLED_DECEASED ─────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('BENEFITS_CANCELLED_DECEASED','EN','ALL',
 'Benefits cancelled following death registration',
 'All active tax benefits for the deceased citizen {deceasedFullName} (NIN: {deceasedNin}) '
 || 'have been automatically cancelled as of {cancelledAt}. '
 || 'If you have questions, contact the State Tax Service.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('BENEFITS_CANCELLED_DECEASED','RU','ALL',
 'Льготы аннулированы в связи со смертью',
 'Все активные налоговые льготы умершего гражданина {deceasedFullName} (ИНН: {deceasedNin}) '
 || 'автоматически аннулированы с {cancelledAt}. '
 || 'По вопросам обращайтесь в Государственную налоговую службу.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('BENEFITS_CANCELLED_DECEASED','TK','ALL',
 'Ölüm hasaba alynandan soň ýeňillikler ýatyryldy',
 'Aradan çykan raýat {deceasedFullName} (şahsy belgisi: {deceasedNin}) üçin ähli işjeň salgyt ýeňillikleri '
 || '{cancelledAt} senesinden başlap awtomatik ýatyryldy. '
 || 'Soraglar üçin Döwlet Salgyt gullugyna ýüz tutuň.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── BUSINESS_OWNER_DECEASED ─────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('BUSINESS_OWNER_DECEASED','EN','ALL',
 'Business suspended: owner deceased',
 'Business {businessName} (registration: {registrationNumber}) has been automatically suspended '
 || 'following the death of its owner {deceasedFullName} on {dateOfDeath}. '
 || 'Please contact the Business Registry to transfer ownership or close the business.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('BUSINESS_OWNER_DECEASED','RU','ALL',
 'Бизнес приостановлен: владелец умер',
 'Деятельность предприятия {businessName} (рег. №: {registrationNumber}) приостановлена '
 || 'в связи со смертью владельца {deceasedFullName} {dateOfDeath}. '
 || 'Обратитесь в Бизнес-реестр для передачи прав или закрытия предприятия.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('BUSINESS_OWNER_DECEASED','TK','ALL',
 'Işewürlik togtadyldy: eýesi aradan çykdy',
 '{businessName} (hasaba alyş №: {registrationNumber}) işewürligi {deceasedFullName} eýesiniň '
 || '{dateOfDeath} senesinde aradan çykmagy bilen baglylykda awtomatik togtadyldy. '
 || 'Eýeçiligi geçirmek ýa-da işewürligi ýapmak üçin Işewürlik Reýestrine ýüz tutuň.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── MARRIAGE_TAX_INFO ────────────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('MARRIAGE_TAX_INFO','EN','ALL',
 'Marital status updated in tax system',
 'Your marital status has been updated to MARRIED in the State Tax System as of {marriageDate}. '
 || 'This may affect your tax bracket and benefit eligibility. '
 || 'Visit the SANLY Tax Portal for details.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('MARRIAGE_TAX_INFO','RU','ALL',
 'Семейное положение обновлено в налоговой системе',
 'Ваше семейное положение обновлено до «В браке» в Государственной налоговой системе с {marriageDate}. '
 || 'Это может повлиять на ваш налоговый вычет и право на льготы. '
 || 'Войдите на Налоговый портал SANLY для получения подробностей.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('MARRIAGE_TAX_INFO','TK','ALL',
 'Maşgala ýagdaýy salgyt ulgamynda täzelendi',
 'Siziň maşgala ýagdaýyňyz {marriageDate} senesinden başlap Döwlet Salgyt ulgamynda "Nikaly" diýip täzelendi. '
 || 'Bu salgyt derejesine we ýeňillik hukugyna täsir edip biler. '
 || 'Jikme-jikler üçin SANLY Salgyt portalyna giriň.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── DIVORCE_TAX_INFO ────────────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DIVORCE_TAX_INFO','EN','ALL',
 'Marital status updated following divorce',
 'Your marital status has been updated to DIVORCED in the State Tax System as of {divorceDate}. '
 || 'This may affect your tax bracket and benefit eligibility. '
 || 'Visit the SANLY Tax Portal for details.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DIVORCE_TAX_INFO','RU','ALL',
 'Семейное положение обновлено после развода',
 'Ваше семейное положение обновлено до «В разводе» в Государственной налоговой системе с {divorceDate}. '
 || 'Это может повлиять на ваш налоговый вычет и право на льготы. '
 || 'Войдите на Налоговый портал SANLY.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('DIVORCE_TAX_INFO','TK','ALL',
 'Aýrylyşandan soň maşgala ýagdaýy täzelendi',
 'Siziň maşgala ýagdaýyňyz {divorceDate} senesinden başlap Döwlet Salgyt ulgamynda "Aýrylyşan" diýip täzelendi. '
 || 'Bu salgyt derejesine we ýeňillik hukugyna täsir edip biler. '
 || 'SANLY Salgyt portalyna giriň.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;
