INSERT INTO service_types (office_code, institution_type, service_name, duration_minutes, requires_documents) VALUES
-- DMV
('TM-OFF-0001','DMV','New License Application',30,'National ID, Medical Clearance, Passport photo'),
('TM-OFF-0001','DMV','License Renewal',30,'Current License, National ID'),
('TM-OFF-0001','DMV','License Category Change',30,'Current License, Medical Clearance'),
-- Civil Registry
('TM-OFF-0002','CIVIL_REGISTRY','Birth Registration',45,'Hospital birth certificate, Parents national IDs'),
('TM-OFF-0002','CIVIL_REGISTRY','Marriage Registration',45,'Both parties national IDs, Medical certificates'),
('TM-OFF-0002','CIVIL_REGISTRY','Death Registration',45,'Medical death certificate, Deceased national ID'),
('TM-OFF-0002','CIVIL_REGISTRY','Name Change',45,'National ID, Court order'),
-- Tax
('TM-OFF-0003','TAX','Tax Consultation',30,'National ID, Tax documents'),
('TM-OFF-0003','TAX','Filing Assistance',30,'National ID, Income documents'),
('TM-OFF-0003','TAX','Business Tax Registration',30,'Business registration certificate, National ID'),
-- Court
('TM-OFF-0004','COURT','Case Filing',30,'National ID, Case documents'),
('TM-OFF-0004','COURT','Document Submission',30,'National ID, Court reference number'),
-- Land Registry
('TM-OFF-0005','LAND_REGISTRY','Property Transfer',60,'Property deed, Both parties national IDs, Tax clearance'),
('TM-OFF-0005','LAND_REGISTRY','New Property Registration',60,'Building permit, Property documents, National ID'),
('TM-OFF-0005','LAND_REGISTRY','Valuation Request',60,'Property deed, National ID'),
-- Social Services
('TM-OFF-0006','SOCIAL_SERVICES','Benefit Application',45,'National ID, Income documents, Family records'),
('TM-OFF-0006','SOCIAL_SERVICES','Pension Consultation',45,'National ID, Employment records')
ON CONFLICT DO NOTHING;
