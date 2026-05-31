CREATE TABLE refresh_token (
    id          UUID        NOT NULL PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    user_id     UUID        NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP   NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_refresh_token_token   ON refresh_token(token);
CREATE INDEX idx_refresh_token_user_id ON refresh_token(user_id);