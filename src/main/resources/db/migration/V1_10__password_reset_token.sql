CREATE TABLE password_reset_token (
    user_id      INTEGER      NOT NULL,
    token        VARCHAR(128) NOT NULL UNIQUE,
    token_expiry TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, token)
);
ALTER TABLE password_reset_token
    ADD FOREIGN KEY (user_id) REFERENCES "user" (id);