ALTER TABLE auth_sessions
    ADD COLUMN refresh_token_hash VARCHAR(64),
    ADD COLUMN previous_refresh_token_hash VARCHAR(64),
    ADD COLUMN refresh_rotated_at TIMESTAMPTZ;

ALTER TABLE auth_sessions ADD CONSTRAINT ck_auth_sessions_refresh_rotation
    CHECK ((previous_refresh_token_hash IS NULL) = (refresh_rotated_at IS NULL));
