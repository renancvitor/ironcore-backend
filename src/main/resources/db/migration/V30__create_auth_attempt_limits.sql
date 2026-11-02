CREATE TABLE auth_attempt_limits (
    subject_hash VARCHAR(64) PRIMARY KEY,
    window_started_at TIMESTAMPTZ NOT NULL,
    attempts INTEGER NOT NULL CHECK (attempts >= 0)
);

CREATE INDEX idx_auth_attempt_limits_window ON auth_attempt_limits (window_started_at);
