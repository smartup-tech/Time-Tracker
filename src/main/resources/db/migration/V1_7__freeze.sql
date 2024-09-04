ALTER TABLE track_unit
    ADD COLUMN frozen BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE freeze_record
(
    id                 SERIAL PRIMARY KEY,
    freeze_date        DATE        NOT NULL,
    user_id            INTEGER     NOT NULL,
    status             VARCHAR(50) NOT NULL,
    info               TEXT,
    created_date       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE freeze_record
    ADD FOREIGN KEY (user_id) REFERENCES "user" (id);

CREATE FUNCTION update_last_modified_date_freeze_record() RETURNS TRIGGER AS
$$
BEGIN
    NEW.last_modified_date = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_last_modified_date_freeze_record_trigger
    BEFORE UPDATE
    ON freeze_record
    FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_date_freeze_record();
