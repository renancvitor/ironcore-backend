ALTER TABLE auth_sessions ADD COLUMN absolute_expires_at TIMESTAMP WITH TIME ZONE;

UPDATE auth_sessions SET absolute_expires_at = expires_at;

ALTER TABLE auth_sessions ALTER COLUMN absolute_expires_at SET NOT NULL;
ALTER TABLE auth_sessions ADD CONSTRAINT ck_auth_session_expiration_limits
    CHECK (expires_at <= absolute_expires_at
        AND absolute_expires_at > created_at
        AND absolute_expires_at <= created_at + INTERVAL '2160 hours');
