CREATE TABLE error_logs (
    id BIGSERIAL PRIMARY KEY,
    error_code VARCHAR(30) NOT NULL,
    message TEXT NOT NULL,
    exception_class VARCHAR(255) NOT NULL,
    request_path VARCHAR(255) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    user_id BIGINT,
    correlation_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_error_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_error_logs_user_id ON error_logs(user_id);
CREATE INDEX idx_error_logs_error_code ON error_logs(error_code);
CREATE INDEX idx_error_logs_created_at ON error_logs(created_at);
CREATE INDEX idx_error_logs_correlation_id ON error_logs(correlation_id);
