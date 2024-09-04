CREATE TABLE production_calendar_day(
    id           BIGSERIAL   PRIMARY KEY,
    day          DATE        NOT NULL UNIQUE,
    status       VARCHAR(50) NOT NULL,
    hours        REAL        NOT NULL,
    created_date TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);