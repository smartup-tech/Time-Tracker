CREATE TABLE notice_schedule
(
    user_id            SERIAL PRIMARY KEY,
    days               integer[] NOT NULL,
    created_date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE notice_schedule
    ADD FOREIGN KEY (user_id) REFERENCES "user" (id);

CREATE FUNCTION update_last_modified_date_notice_schedule() RETURNS TRIGGER AS
$$
BEGIN
    NEW.last_modified_date = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_last_modified_date_notice_schedule_trigger
    BEFORE UPDATE
    ON notice_schedule
    FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_date_notice_schedule();

CREATE TABLE notice_schedule_done
(
    id   SERIAL PRIMARY KEY,
    date TIMESTAMP NOT NULL
);

CREATE INDEX notice_user_id_idx ON notice (user_id);