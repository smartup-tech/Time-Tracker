CREATE TABLE notice
(
    id                 BIGSERIAL PRIMARY KEY,
    type               VARCHAR(50) NOT NULL,
    user_id            INTEGER     NOT NULL,
    text               TEXT,
    data               JSONB,
    read               BOOLEAN     NOT NULL DEFAULT FALSE,
    created_by         INTEGER     NOT NULL DEFAULT 0,
    created_date       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE notice
    ADD FOREIGN KEY (user_id) REFERENCES "user" (id);

CREATE FUNCTION update_last_modified_date_notice() RETURNS TRIGGER AS
$$
BEGIN
    NEW.last_modified_date = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_last_modified_date_notice_trigger
    BEFORE UPDATE
    ON notice
    FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_date_notice();

CREATE INDEX user_role_role_id_idx ON user_role (role_id);