-- rename fields "paid" to "billable"
ALTER TABLE task RENAME COLUMN paid TO billable;
ALTER TABLE track_unit RENAME COLUMN paid TO billable;