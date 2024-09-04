ALTER TABLE track_unit
    DROP CONSTRAINT track_unit_hours_check;

ALTER TABLE track_unit
    ADD CONSTRAINT track_unit_hours_check CHECK (hours >= 0);