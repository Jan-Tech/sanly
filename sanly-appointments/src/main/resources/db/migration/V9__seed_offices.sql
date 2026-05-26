INSERT INTO government_offices (office_code, institution_type, name, region, address, phone, status) VALUES
('TM-OFF-0001', 'DMV', 'DMV Ashgabat Central', 'Ashgabat', 'Bitarap Türkmenistan Avenue 15, Ashgabat', '+99312123456', 'ACTIVE'),
('TM-OFF-0002', 'CIVIL_REGISTRY', 'Civil Registry Ashgabat', 'Ashgabat', 'Garaşsyzlyk Avenue 22, Ashgabat', '+99312234567', 'ACTIVE'),
('TM-OFF-0003', 'TAX', 'Tax Authority Ashgabat', 'Ashgabat', 'Magtymguly Avenue 45, Ashgabat', '+99312345678', 'ACTIVE'),
('TM-OFF-0004', 'COURT', 'Ashgabat District Court', 'Ashgabat', 'Oguzhan Street 7, Ashgabat', '+99312456789', 'ACTIVE'),
('TM-OFF-0005', 'LAND_REGISTRY', 'Land Registry Ashgabat', 'Ashgabat', 'Ataturk Avenue 12, Ashgabat', '+99312567890', 'ACTIVE'),
('TM-OFF-0006', 'SOCIAL_SERVICES', 'Social Services Ashgabat', 'Ashgabat', 'Nurmuhammet Andalib Street 5, Ashgabat', '+99312678901', 'ACTIVE')
ON CONFLICT (office_code) DO NOTHING;
