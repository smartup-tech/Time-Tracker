CREATE UNIQUE INDEX freeze_record_index ON freeze_record(freeze_date);

ALTER TABLE freeze_record ADD CONSTRAINT freeze_date UNIQUE USING INDEX freeze_record_index;