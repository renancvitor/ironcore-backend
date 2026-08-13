CREATE TABLE workout_cycles(
    id BIGSERIAL PRIMARY KEY,
    person_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    training_goal_id BIGINT NOT NULL,
    start_date DATE,
    end_date DATE,
    desired_duration_months INTEGER,
    workout_status VARCHAR(50) NOT NULL,
    workout_origin VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    CONSTRAINT fk_person_workout_cycle FOREIGN KEY (person_id) REFERENCES persons(id) ON DELETE CASCADE,
    CONSTRAINT fk_training_goal_workout_cycle FOREIGN KEY (training_goal_id) REFERENCES training_goals(id)
);