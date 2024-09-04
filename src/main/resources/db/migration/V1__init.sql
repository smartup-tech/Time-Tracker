--1. Position
CREATE TABLE position
(
    id                 SERIAL       PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    external_rate      NUMERIC      NOT NULL,
    is_archived        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_date       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX position_name_idx ON position (name);
ALTER TABLE position
    ADD CONSTRAINT position_external_rate_check CHECK (external_rate >= 0);
--function for position
CREATE FUNCTION update_last_modified_date_position() RETURNS TRIGGER AS
$$
BEGIN
    NEW.last_modified_date = now();
    RETURN NEW;
END;
$$ language 'plpgsql';
--trigger for position
CREATE TRIGGER update_last_modified_date_position_trigger
    BEFORE UPDATE
    ON position
    FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_date_position();

--2. Project
CREATE TABLE project
(
    id                 SERIAL       PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    is_archived        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_date       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX project_name_idx ON project (name);
--function for project
CREATE FUNCTION update_last_modified_date_project() RETURNS TRIGGER AS
$$
BEGIN
    NEW.last_modified_date = now();
    RETURN NEW;
END;
$$ language 'plpgsql';
--trigger for project
CREATE TRIGGER update_last_modified_date_project_trigger
    BEFORE UPDATE
    ON project
    FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_date_project();

--3. Roles in project
CREATE TABLE project_role
(
    id   VARCHAR(50)  PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
ALTER TABLE project_role
    ADD CONSTRAINT project_role_id_check CHECK (id = 'MANAGER' OR id = 'EMPLOYEE');

--4. Task
CREATE TABLE task
(
    id                 BIGSERIAL    PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    project_id         INTEGER      NOT NULL,
    paid               BOOLEAN      NOT NULL DEFAULT TRUE,
    is_archived        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_date       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE task
    ADD FOREIGN KEY (project_id) REFERENCES project (id);
CREATE UNIQUE INDEX task_name_project_id_idx ON task (name, project_id);
CREATE INDEX task_project_id_idx ON task (project_id);
--function for task
CREATE FUNCTION update_last_modified_date_task() RETURNS TRIGGER AS
$$
BEGIN
    NEW.last_modified_date = now();
    RETURN NEW;
END;
$$ language 'plpgsql';
--trigger for task
CREATE TRIGGER update_last_modified_date_task_trigger
    BEFORE UPDATE
    ON task
    FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_date_task();

--5. User
CREATE TABLE "user"
(
    id                 SERIAL       PRIMARY KEY,
    first_name         VARCHAR(255) NOT NULL,
    middle_name        VARCHAR(255),
    last_name          VARCHAR(255) NOT NULL,
    email              VARCHAR(50)  NOT NULL,
    password_hash      VARCHAR(100) NOT NULL,
    position_id        INTEGER      NOT NULL,
    is_archived        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_date       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE "user"
    ADD FOREIGN KEY (position_id) REFERENCES position (id);
CREATE UNIQUE INDEX user_email_idx ON "user" (email);
--function for user
CREATE FUNCTION update_last_modified_date_user() RETURNS TRIGGER AS
$$
BEGIN
    NEW.last_modified_date = now();
    RETURN NEW;
END;
$$ language 'plpgsql';
--trigger for user
CREATE TRIGGER update_last_modified_date_user_trigger
    BEFORE UPDATE
    ON "user"
    FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_date_user();

--6. Role
CREATE TABLE role
(
    id   VARCHAR(50)  PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
ALTER TABLE role
    ADD CONSTRAINT role_id_check CHECK (id = 'ROLE_ADMIN' OR id = 'ROLE_USER' or id = 'ROLE_REPORT_RECEIVER');

--7. Links of users with roles
CREATE TABLE user_role
(
    user_id INTEGER     NOT NULL,
    role_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role_id)
);
ALTER TABLE user_role
    ADD FOREIGN KEY (user_id) REFERENCES "user" (id);
ALTER TABLE user_role
    ADD FOREIGN KEY (role_id) REFERENCES role (id);

--8. Links of users with projects
CREATE TABLE user_project_role
(
    user_id         INTEGER     NOT NULL,
    project_id      INTEGER     NOT NULL,
    project_role_id VARCHAR(50) NOT NULL,
    external_rate   NUMERIC,
    PRIMARY KEY (user_id, project_id, project_role_id)
);
ALTER TABLE user_project_role
    ADD FOREIGN KEY (user_id) REFERENCES "user" (id);
ALTER TABLE user_project_role
    ADD FOREIGN KEY (project_id) REFERENCES project (id);
ALTER TABLE user_project_role
    ADD FOREIGN KEY (project_role_id) REFERENCES project_role (id);
ALTER TABLE user_project_role
    ADD CONSTRAINT user_project_role_external_rate_check CHECK (external_rate > 0);

--9. Track_unit
CREATE TABLE track_unit
(
    id                 BIGSERIAL   PRIMARY KEY,
    work_day           DATE        NOT NULL,
    status             VARCHAR(50) NOT NULL,
    hours              REAL        NOT NULL,
    comment            TEXT,
    rate               REAL        NOT NULL DEFAULT 1,
    paid               BOOLEAN     NOT NULL DEFAULT TRUE,
    task_id            BIGINT      NOT NULL,
    user_id            INTEGER     NOT NULL,
    created_date       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE track_unit
    ADD FOREIGN KEY (task_id) REFERENCES task (id);
ALTER TABLE track_unit
    ADD FOREIGN KEY (user_id) REFERENCES "user" (id);
ALTER TABLE track_unit
    ADD CONSTRAINT track_unit_status_check
        CHECK (status = 'CREATED' OR status = 'SUBMITTED' OR status = 'APPROVED' OR status = 'REJECTED');
ALTER TABLE track_unit
    ADD CONSTRAINT track_unit_hours_check CHECK (hours > 0);
ALTER TABLE track_unit
    ADD CONSTRAINT track_unit_rate_check CHECK (rate = 1 OR rate = 1.5 OR rate = 2);
--function for track_unit
CREATE FUNCTION update_last_modified_date_track_unit() RETURNS TRIGGER AS
$$
BEGIN
    NEW.last_modified_date = now();
    RETURN NEW;
END;
$$ language 'plpgsql';
--trigger for track_unit
CREATE TRIGGER update_last_modified_date_track_unit_trigger
    BEFORE UPDATE
    ON track_unit
    FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_date_track_unit();

--Add initial data to DB
INSERT INTO position(name, external_rate)
VALUES ('None', 0);

INSERT INTO role(id, name)
VALUES ('ROLE_ADMIN', 'Администратор');
INSERT INTO role(id, name)
VALUES ('ROLE_USER', 'Пользователь');
INSERT INTO role(id, name)
VALUES ('ROLE_REPORT_RECEIVER', 'Получатель отчетов');

-- base admin, which should be not archived; password: admin
INSERT INTO "user"(first_name, last_name, email, password_hash, position_id)
VALUES ('admin', 'admin', 'admin@smartup.ru', '$2y$10$3XCy114Ep7LCnTFqKE8B4OyD7XR3mu/ziGVB8XWYKWRx.sxFXmOe2', 1);

INSERT INTO user_role(user_id, role_id)
VALUES (1, 'ROLE_ADMIN');

INSERT INTO project_role(id, name)
VALUES ('MANAGER', 'Менеджер проекта');
INSERT INTO project_role(id, name)
VALUES ('EMPLOYEE', 'Пользователь');
