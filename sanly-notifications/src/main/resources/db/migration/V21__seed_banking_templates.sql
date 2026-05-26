-- Banking API consent notification templates (EN / TK / RU)
-- Priority: SMS channel for BANKING_CONSENT_REQUESTED (time-sensitive)

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template)
VALUES

-- BANKING_CONSENT_REQUESTED (SMS — time sensitive, 10-minute window)
('BANKING_CONSENT_REQUESTED', 'EN', 'SMS',
 'SANLY: Bank Data Request',
 '{{bankName}} requests access to your government data. Purpose: {{purpose}}. Scopes: {{scopeList}}. Log into SANLY portal to approve or reject within 10 minutes.'),

('BANKING_CONSENT_REQUESTED', 'TK', 'SMS',
 'SANLY: Bank maglumat sorowy',
 '{{bankName}} siziň döwlet maglumatyňyza girişi soraýar. Maksat: {{purpose}}. 10 minut içinde SANLY portalyna girip tassyklaň ýa-da ret ediň.'),

('BANKING_CONSENT_REQUESTED', 'RU', 'SMS',
 'SANLY: Запрос банка',
 '{{bankName}} запрашивает доступ к вашим государственным данным. Цель: {{purpose}}. Войдите на портал SANLY в течение 10 минут для подтверждения или отклонения.'),

('BANKING_CONSENT_REQUESTED', 'EN', 'IN_APP',
 'Bank Data Request — {{bankName}}',
 '{{bankName}} is requesting access to your government data for: {{purpose}}. Requested scopes: {{scopeList}}. Log into the SANLY portal to approve or reject. This request expires in 10 minutes.'),

('BANKING_CONSENT_REQUESTED', 'TK', 'IN_APP',
 'Bank maglumat sorowy — {{bankName}}',
 '{{bankName}} şu maksat bilen döwlet maglumatyňyza girişi soraýar: {{purpose}}. Soran ugurlary: {{scopeList}}. Tassyklamak ýa-da ret etmek üçin SANLY portalyna giriň. Bu sorow 10 minutdan soň möhleti geçer.'),

('BANKING_CONSENT_REQUESTED', 'RU', 'IN_APP',
 'Запрос банка на данные — {{bankName}}',
 '{{bankName}} запрашивает доступ к вашим государственным данным. Цель: {{purpose}}. Запрошенные разделы: {{scopeList}}. Войдите на портал SANLY для подтверждения или отклонения. Запрос истекает через 10 минут.'),

-- BANKING_CONSENT_APPROVED
('BANKING_CONSENT_APPROVED', 'EN', 'IN_APP',
 'Data Access Approved — {{bankName}}',
 'You approved {{bankName}} to access your data ({{scopeList}}). They have 1 hour to retrieve it. View and manage all bank data access in your portal under Banking & Consent.'),

('BANKING_CONSENT_APPROVED', 'TK', 'IN_APP',
 'Maglumat girimesi tassyklandi — {{bankName}}',
 'Siz {{bankName}} bankynyň maglumatyňyza girişini tassykladyňyz ({{scopeList}}). Olar 1 sagat içinde ony almaga mümkinçilik alarlar. SANLY portalynyň Bank we Razylyk bölüminde ähli bank maglumat girişini görüp bilersiňiz.'),

('BANKING_CONSENT_APPROVED', 'RU', 'IN_APP',
 'Доступ к данным одобрен — {{bankName}}',
 'Вы одобрили доступ {{bankName}} к вашим данным ({{scopeList}}). У них есть 1 час для их получения. Управляйте доступом банков к данным в разделе «Банкинг и согласие» портала SANLY.'),

-- BANKING_CONSENT_REJECTED
('BANKING_CONSENT_REJECTED', 'EN', 'IN_APP',
 'Data Access Rejected',
 'You rejected {{bankName}}''s request to access your government data. No data was shared. The bank has been notified.'),

('BANKING_CONSENT_REJECTED', 'TK', 'IN_APP',
 'Maglumat girimesi ret edildi',
 'Siz {{bankName}} bankynyň döwlet maglumatyňyza girişini ret etdiňiz. Hiç hili maglumat paýlaşylmady. Bank habarly edildi.'),

('BANKING_CONSENT_REJECTED', 'RU', 'IN_APP',
 'Доступ к данным отклонён',
 'Вы отклонили запрос {{bankName}} на доступ к вашим государственным данным. Никакие данные не были переданы. Банк уведомлён.'),

-- BANKING_CONSENT_EXPIRED
('BANKING_CONSENT_EXPIRED', 'EN', 'IN_APP',
 'Consent Request Expired — {{bankName}}',
 'A data access request from {{bankName}} expired without your response. No data was shared. If this request was unexpected, review your access history in the Banking section.'),

('BANKING_CONSENT_EXPIRED', 'TK', 'IN_APP',
 'Razylyk sorowy möhleti geçdi — {{bankName}}',
 '{{bankName}} bankynyň maglumat girişi sorowy siziň jogabyňyzsyz möhleti geçdi. Hiç hili maglumat paýlaşylmady.'),

('BANKING_CONSENT_EXPIRED', 'RU', 'IN_APP',
 'Запрос на согласие истёк — {{bankName}}',
 'Запрос {{bankName}} на доступ к данным истёк без вашего ответа. Никакие данные не были переданы.'),

-- BANKING_DATA_ACCESSED
('BANKING_DATA_ACCESSED', 'EN', 'IN_APP',
 'Your Data Was Accessed — {{bankName}}',
 '{{bankName}} accessed your government data on {{accessedAt}}. Data shared: {{scopeList}}. If you did not authorise this access, contact support immediately through the SANLY portal.'),

('BANKING_DATA_ACCESSED', 'TK', 'IN_APP',
 'Maglumatyňyza girildi — {{bankName}}',
 '{{bankName}} {{accessedAt}} senesinde döwlet maglumatyňyza girdi. Paýlaşylan maglumatlar: {{scopeList}}. Bu girişi ygtyýarlandyrmasaňyz, SANLY portaly arkaly habar beriň.'),

('BANKING_DATA_ACCESSED', 'RU', 'IN_APP',
 'К вашим данным получен доступ — {{bankName}}',
 '{{bankName}} получил доступ к вашим государственным данным {{accessedAt}}. Переданные данные: {{scopeList}}. Если вы не авторизовали этот доступ, немедленно обратитесь в службу поддержки через портал SANLY.')

ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;
