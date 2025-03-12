ALTER TABLE "user" RENAME TO employee;
ALTER TABLE employee RENAME CONSTRAINT user_pkey TO employee_pk;
ALTER TABLE employee RENAME CONSTRAINT user_position_id_fkey TO employee_position_id_fkey;
ALTER INDEX user_email_idx RENAME TO employee_email_idx;
ALTER TRIGGER update_last_modified_date_user_trigger ON employee RENAME TO update_last_modified_date_employee_trigger;
ALTER TRIGGER user_audit_trail ON employee RENAME TO employee_audit_trail;

ALTER TABLE user_project_role RENAME TO employee_project_role;
ALTER TABLE employee_project_role RENAME COLUMN user_id TO employee_id;
ALTER TABLE employee_project_role RENAME CONSTRAINT user_project_role_pkey TO employee_project_role_pkey;
ALTER TABLE employee_project_role RENAME CONSTRAINT user_project_role_project_id_fkey TO employee_project_role_project_id_fkey;
ALTER TABLE employee_project_role RENAME CONSTRAINT user_project_role_project_role_id_fkey TO employee_project_role_project_role_id_fkey;
ALTER TABLE employee_project_role RENAME CONSTRAINT user_project_role_user_id_fkey TO employee_project_role_employee_id_fkey;
ALTER TABLE employee_project_role RENAME CONSTRAINT user_project_role_external_rate_check TO employee_project_role_external_rate_check;
ALTER TRIGGER user_project_role_audit_trail ON employee_project_role RENAME TO employee_project_role_audit_trail;

ALTER TABLE user_role RENAME TO employee_role;
ALTER TABLE employee_role RENAME COLUMN user_id TO employee_id;
ALTER TABLE employee_role RENAME CONSTRAINT user_role_pkey TO employee_role_pkey;
ALTER TABLE employee_role RENAME CONSTRAINT user_role_role_id_fkey TO employee_role_role_id_fkey;
ALTER TABLE employee_role RENAME CONSTRAINT user_role_user_id_fkey TO employee_role_employee_id_fkey;
ALTER INDEX user_role_role_id_idx RENAME TO employee_role_role_id_idx;
ALTER TRIGGER user_role_audit_trail ON employee_role RENAME TO employee_role_audit_trail;

ALTER TABLE audit_trail RENAME COLUMN user_id TO employee_id;

ALTER TABLE freeze_record RENAME COLUMN user_id TO employee_id;
ALTER TABLE freeze_record RENAME CONSTRAINT freeze_record_user_id_fkey TO freeze_record_employee_id_fkey;

ALTER TABLE notice RENAME COLUMN user_id TO employee_id;
ALTER TABLE notice RENAME CONSTRAINT notice_user_id_fkey TO notice_employee_id_fkey;
ALTER INDEX notice_user_id_idx RENAME TO notice_employee_id_idx;

ALTER TABLE password_reset_token RENAME COLUMN user_id TO employee_id;
ALTER TABLE password_reset_token RENAME CONSTRAINT password_reset_token_user_id_fkey TO password_reset_token_employee_id_fkey;

ALTER TABLE track_unit RENAME COLUMN user_id TO employee_id;
ALTER TABLE track_unit RENAME CONSTRAINT track_unit_user_id_fkey TO track_unit_employee_id_fkey;
ALTER INDEX track_unit_user_id_task_id_work_day_idx RENAME TO track_unit_employee_id_task_id_work_day_idx;

ALTER TABLE tracked_project_task RENAME COLUMN user_id TO employee_id;
ALTER TABLE tracked_project_task RENAME CONSTRAINT tracked_project_task_user_id_fkey TO tracked_project_task_employee_id_fkey;

