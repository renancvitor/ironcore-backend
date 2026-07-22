CREATE TABLE exercise_muscle_targets (
    id BIGSERIAL PRIMARY KEY,
    exercise_id BIGINT NOT NULL,
    muscle_subgroup_id BIGINT NOT NULL,
    target_role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    CONSTRAINT fk_exercise_muscle_targets_exercise FOREIGN KEY (exercise_id) REFERENCES exercises (id),
    CONSTRAINT fk_exercise_muscle_targets_muscle_subgroup FOREIGN KEY (muscle_subgroup_id) REFERENCES muscle_subgroups (id),

    CONSTRAINT uk_exercise_muscle_targets_association UNIQUE (exercise_id, muscle_subgroup_id)
);