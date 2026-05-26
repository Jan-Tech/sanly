-- Seed default Turkmenistan customs ports
INSERT INTO customs_ports (port_code, name, port_type, region, status) VALUES
('TM-PORT-001', 'Turkmenbashi International Seaport',   'SEAPORT',      'Balkan',     'ACTIVE'),
('TM-PORT-002', 'Ashgabat International Airport',       'AIRPORT',      'Ahal',       'ACTIVE'),
('TM-PORT-003', 'Farap Land Border Crossing',            'LAND_BORDER',  'Lebap',      'ACTIVE'),
('TM-PORT-004', 'Sarahs Land Border Crossing',           'LAND_BORDER',  'Mary',       'ACTIVE'),
('TM-PORT-005', 'Imamnazar Land Border Crossing',        'LAND_BORDER',  'Dashoguz',   'ACTIVE'),
('TM-PORT-006', 'Turkmenabad Railway Terminal',          'RAILWAY',      'Lebap',      'ACTIVE'),
('TM-PORT-007', 'Ashgabat Inland Customs Depot',        'INLAND_DEPOT', 'Ahal',       'ACTIVE');
