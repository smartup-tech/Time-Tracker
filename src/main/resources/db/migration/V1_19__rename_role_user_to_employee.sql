ALTER TABLE role DROP CONSTRAINT role_id_check;
ALTER TABLE employee_role DROP CONSTRAINT employee_role_role_id_fkey;

UPDATE employee_role
SET role_id = 'ROLE_EMPLOYEE'
WHERE role_id = 'ROLE_USER';

UPDATE role
SET id = 'ROLE_EMPLOYEE'
WHERE id = 'ROLE_USER';

ALTER TABLE role
    ADD CONSTRAINT role_id_check CHECK (id = 'ROLE_ADMIN' OR id = 'ROLE_EMPLOYEE' or id = 'ROLE_REPORT_RECEIVER');

ALTER TABLE employee_role
    ADD CONSTRAINT employee_role_role_id_fkey FOREIGN KEY (role_id) REFERENCES role(id);