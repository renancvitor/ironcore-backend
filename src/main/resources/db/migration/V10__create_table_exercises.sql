CREATE TABLE exercises(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    equipment_type_id BIGINT NOT NULL,
    activity_type_id BIGINT NOT NULL,
    unilateral BOOLEAN NOT NULL,
    compound BOOLEAN NOT NULL,
    suggested_rest_seconds INTEGER,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    CONSTRAINT fk_equipment_types_exercises FOREIGN KEY (equipment_type_id) REFERENCES equipment_types(id),
    CONSTRAINT fk_activity_types_exercises FOREIGN KEY (activity_type_id) REFERENCES activity_types(id)
);