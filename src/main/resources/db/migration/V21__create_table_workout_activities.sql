CREATE TABLE workout_activities(
    id BIGSERIAL PRIMARY KEY,
    workout_day_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    order_index INTEGER NOT NULL,
    sets INTEGER,
    rep_range_min INTEGER,
    rep_range_max INTEGER,
    target_load_kg NUMERIC(5,2),
    target_load_text VARCHAR(100),
    duration_minutes INTEGER,
    distance_km NUMERIC(5,2),
    intensity_text VARCHAR(100),
    rest_seconds INTEGER,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    CONSTRAINT workout_activity_workout_day FOREIGN KEY (workout_day_id) REFERENCES workout_days(id) ON DELETE CASCADE,
    CONSTRAINT workout_activity_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id),

    CONSTRAINT uk_workout_activities_day_order UNIQUE (workout_day_id, order_index) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uk_workout_activities_day_exercise UNIQUE (workout_day_id, exercise_id)
);