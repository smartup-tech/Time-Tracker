CREATE OR REPLACE FUNCTION update_last_modified_date_track_unit() RETURNS TRIGGER AS
$$
BEGIN
    IF (NEW.hours <> OLD.hours OR NEW.comment <> OLD.comment OR NEW.billable <> OLD.billable
        OR NEW.rate <> OLD.rate OR NEW.status <> OLD.status)
    THEN
        NEW.last_modified_date = now();
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TABLE audit_trail
(
    start_time TIMESTAMP   NOT NULL,
    user_id    INTEGER,
    table_name VARCHAR(50) NOT NULL,
    operation  CHAR(1)     NOT NULL,
    old_val    TEXT,
    new_val    TEXT
);

CREATE OR REPLACE FUNCTION record_audit_trail() RETURNS TRIGGER AS
$$
DECLARE
    user_id INTEGER;
    old_val JSONB;
    new_val JSONB;
BEGIN
    IF ((tg_op <> 'UPDATE') AND (tg_op <> 'INSERT') AND (tg_op <> 'DELETE'))
    THEN
        RETURN NULL;
    END IF;

    user_id = current_setting('session.user_id', true)::INTEGER;
    old_val = row_to_json(OLD);
    new_val = row_to_json(NEW);

    IF ((old_val ->> 'last_modified_date' is null)
        OR (new_val ->> 'last_modified_date' is null)
        OR (old_val ->> 'last_modified_date' <> new_val ->> 'last_modified_date'))
    THEN
        INSERT INTO audit_trail
        SELECT now(), user_id, tg_table_name, substring(tg_op for 1), old_val, new_val;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER position_audit_trail
    AFTER INSERT OR UPDATE OR DELETE
    ON position
    FOR EACH ROW
EXECUTE PROCEDURE record_audit_trail();

CREATE TRIGGER project_audit_trail
    AFTER INSERT OR UPDATE OR DELETE
    ON project
    FOR EACH ROW
EXECUTE PROCEDURE record_audit_trail();

CREATE TRIGGER task_audit_trail
    AFTER INSERT OR UPDATE OR DELETE
    ON task
    FOR EACH ROW
EXECUTE PROCEDURE record_audit_trail();

CREATE TRIGGER track_unit_audit_trail
    AFTER INSERT OR UPDATE OR DELETE
    ON track_unit
    FOR EACH ROW
EXECUTE PROCEDURE record_audit_trail();

CREATE TRIGGER user_audit_trail
    AFTER INSERT OR UPDATE OR DELETE
    ON "user"
    FOR EACH ROW
EXECUTE PROCEDURE record_audit_trail();

CREATE TRIGGER user_project_role_audit_trail
    AFTER INSERT OR UPDATE OR DELETE
    ON user_project_role
    FOR EACH ROW
EXECUTE PROCEDURE record_audit_trail();

CREATE TRIGGER user_role_audit_trail
    AFTER INSERT OR UPDATE OR DELETE
    ON user_role
    FOR EACH ROW
EXECUTE PROCEDURE record_audit_trail();