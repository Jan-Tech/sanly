DO $$
DECLARE
    offices TEXT[] := ARRAY['TM-OFF-0001','TM-OFF-0002','TM-OFF-0003','TM-OFF-0004','TM-OFF-0005','TM-OFF-0006'];
    days TEXT[] := ARRAY['MON','TUE','WED','THU','FRI'];
    o TEXT; d TEXT;
BEGIN
    FOREACH o IN ARRAY offices LOOP
        FOREACH d IN ARRAY days LOOP
            INSERT INTO availability_schedules (office_code, day_of_week, open_time, close_time, slot_duration_minutes, max_concurrent_appointments)
            VALUES (o, d, '08:00', '17:00', 30, 3)
            ON CONFLICT (office_code, day_of_week) DO NOTHING;
        END LOOP;
    END LOOP;
END $$;
