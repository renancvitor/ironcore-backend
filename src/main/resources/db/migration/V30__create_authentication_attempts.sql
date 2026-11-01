CREATE TABLE authentication_attempts (
    id VARCHAR(64) PRIMARY KEY,
    window_started_at TIMESTAMPTZ NOT NULL,
    attempts INTEGER NOT NULL CHECK (attempts >= 0),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_authentication_attempts_window ON authentication_attempts(window_started_at);
