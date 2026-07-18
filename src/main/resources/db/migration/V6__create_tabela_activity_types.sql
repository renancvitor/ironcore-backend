CREATE TABLE activity_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL,
    sort_order INTEGER NOT NULL
);