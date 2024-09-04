CREATE UNIQUE INDEX track_unit_user_id_task_id_work_day_idx ON track_unit (user_id, task_id, work_day);
CREATE INDEX track_unit_work_day_idx ON track_unit (work_day);