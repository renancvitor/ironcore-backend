CREATE TABLE muscle_subgroups (
    id BIGSERIAL PRIMARY KEY,
    muscle_group_id BIGINT NOT NULL,
    code VARCHAR(80) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL,
    sort_order INTEGER NOT NULL,

    CONSTRAINT fk_muscle_subgroups_muscle_groups FOREIGN KEY (muscle_group_id) REFERENCES muscle_groups(id)
);