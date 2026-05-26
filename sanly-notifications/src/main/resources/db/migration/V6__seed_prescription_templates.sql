-- Seed templates for prescription events (3 languages each)

-- ─── PRESCRIPTION_ISSUED ──────────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('PRESCRIPTION_ISSUED', 'EN', 'ALL',
 'Prescription issued: {medicationName}',
 'Dr. {doctorName} at {clinicName} has issued a prescription for {medicationName} ({dosage}). '
 || 'Prescription code: {prescriptionCode}. Valid until: {expiresAt}. '
 || 'Present this code at any registered pharmacy.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('PRESCRIPTION_ISSUED', 'RU', 'ALL',
 'Выписан рецепт: {medicationName}',
 'Врач {doctorName} в {clinicName} выписал рецепт на {medicationName} ({dosage}). '
 || 'Код рецепта: {prescriptionCode}. Действителен до: {expiresAt}. '
 || 'Предъявите этот код в любой зарегистрированной аптеке.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('PRESCRIPTION_ISSUED', 'TK', 'ALL',
 'Resept berildi: {medicationName}',
 '{clinicName} klinikasyndaky Dr. {doctorName} {medicationName} ({dosage}) üçin resept berdi. '
 || 'Resept kody: {prescriptionCode}. {expiresAt} senesine çenli hereket edýär. '
 || 'Bu kody islendik hasaba alnan dermanhana görkeziň.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

-- ─── PRESCRIPTION_DISPENSED ───────────────────────────────────────────────

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('PRESCRIPTION_DISPENSED', 'EN', 'ALL',
 'Medication dispensed: {medicationName}',
 '{pharmacyName} has dispensed {quantityDispensed} of {medicationName} against prescription {prescriptionCode} on {dispensedAt}. '
 || 'If you did not collect this medication, please contact the Data Protection Authority immediately.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('PRESCRIPTION_DISPENSED', 'RU', 'ALL',
 'Лекарство выдано: {medicationName}',
 '{pharmacyName} выдала {quantityDispensed} {medicationName} по рецепту {prescriptionCode} {dispensedAt}. '
 || 'Если вы не получали это лекарство, немедленно обратитесь в Орган по защите данных.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template) VALUES
('PRESCRIPTION_DISPENSED', 'TK', 'ALL',
 'Derman berildi: {medicationName}',
 '{pharmacyName} {dispensedAt} senesinde {prescriptionCode} reseptine görä {quantityDispensed} {medicationName} berdi. '
 || 'Bu dermany almasaňyz, derrew Maglumat Goragy Edarasyna ýüz tutuň.')
ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;
