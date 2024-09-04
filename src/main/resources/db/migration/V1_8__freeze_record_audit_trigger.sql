CREATE TRIGGER freeze_record_audit_trail
    AFTER INSERT OR UPDATE OR DELETE
    ON freeze_record
    FOR EACH ROW
EXECUTE PROCEDURE record_audit_trail();