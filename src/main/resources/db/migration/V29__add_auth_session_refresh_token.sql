ALTER TABLE auth_sessions ADD COLUMN refresh_token_hash VARCHAR(64);

ALTER TABLE auth_sessions ADD CONSTRAINT ck_auth_sessions_refresh_token_hash
    CHECK (refresh_token_hash IS NULL OR refresh_token_hash ~ '^[0-9a-f]{64}$');

CREATE UNIQUE INDEX uq_auth_sessions_refresh_token_hash ON auth_sessions (refresh_token_hash);
