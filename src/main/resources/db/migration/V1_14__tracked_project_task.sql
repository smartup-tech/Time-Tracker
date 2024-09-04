CREATE TABLE tracked_project_task (
    user_id      INTEGER      NOT NULL,
    task_id      BIGINT   NOT NULL,
    PRIMARY KEY (user_id, task_id)
);
ALTER TABLE tracked_project_task
    ADD FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE;
ALTER TABLE tracked_project_task
    ADD FOREIGN KEY (task_id) REFERENCES "task" (id) ON DELETE CASCADE;