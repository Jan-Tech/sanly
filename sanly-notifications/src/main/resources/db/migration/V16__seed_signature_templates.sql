-- Digital Signature notification templates (EN / TK / RU)

INSERT INTO notification_templates (event_type, language, channel, title_template, body_template)
VALUES
-- DOCUMENT_SIGNED
('DOCUMENT_SIGNED', 'EN', 'IN_APP',
 'Document Signed — {{signatureCode}}',
 'You have digitally signed a document. Signature code: {{signatureCode}}. Purpose: {{purpose}}. If you did not sign this document, revoke it immediately in the SANLY portal.'),

('DOCUMENT_SIGNED', 'TK', 'IN_APP',
 'Resminama gol çekildi — {{signatureCode}}',
 'Siz resminamä sanly gol çekdiňiz. Gol kody: {{signatureCode}}. Maksat: {{purpose}}. Eger bu resminamä gol çekmediňiz, derrew SANLY portalynda ýatyryň.'),

('DOCUMENT_SIGNED', 'RU', 'IN_APP',
 'Документ подписан — {{signatureCode}}',
 'Вы подписали документ цифровой подписью. Код подписи: {{signatureCode}}. Назначение: {{purpose}}. Если вы не подписывали этот документ, немедленно отзовите его на портале SANLY.'),

-- SIGNATURE_REVOKED
('SIGNATURE_REVOKED', 'EN', 'IN_APP',
 'Signature Revoked — {{signatureCode}}',
 'Your digital signature {{signatureCode}} has been revoked. Reason: {{reason}}.'),

('SIGNATURE_REVOKED', 'TK', 'IN_APP',
 'Gol ýatyryldy — {{signatureCode}}',
 'Siziň sanly golyňyz {{signatureCode}} ýatyryldy. Sebäbi: {{reason}}.'),

('SIGNATURE_REVOKED', 'RU', 'IN_APP',
 'Подпись отозвана — {{signatureCode}}',
 'Ваша цифровая подпись {{signatureCode}} была отозвана. Причина: {{reason}}.'),

-- SIGNATURE_VERIFIED_BY_THIRD_PARTY
('SIGNATURE_VERIFIED_BY_THIRD_PARTY', 'EN', 'IN_APP',
 'Your Signature Was Verified',
 'Your signature {{signatureCode}} was verified by a third party on {{verifiedAt}}. If this is unexpected, review your signatures in the SANLY portal.'),

('SIGNATURE_VERIFIED_BY_THIRD_PARTY', 'TK', 'IN_APP',
 'Golyňyz barlandy',
 'Golyňyz {{signatureCode}} {{verifiedAt}} taryhynda üçünji tarap tarapyndan barlandy. Bu garaşylmadyk bolsa, SANLY portalynda gollarňyzy barlaň.'),

('SIGNATURE_VERIFIED_BY_THIRD_PARTY', 'RU', 'IN_APP',
 'Ваша подпись была проверена',
 'Ваша подпись {{signatureCode}} была проверена третьей стороной {{verifiedAt}}. Если это неожиданно, проверьте свои подписи на портале SANLY.')

ON CONFLICT ON CONSTRAINT uq_template_event_lang_channel DO NOTHING;
