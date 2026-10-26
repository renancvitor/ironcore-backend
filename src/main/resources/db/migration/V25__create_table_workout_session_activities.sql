CREATE TABLE workout_session_activities (
    id BIGSERIAL PRIMARY KEY,
    workout_session_id BIGINT NOT NULL REFERENCES workout_sessions(id) ON DELETE CASCADE,
    workout_activity_id BIGINT REFERENCES workout_activities(id) ON DELETE SET NULL,
    exercise_id BIGINT REFERENCES exercises(id) ON DELETE SET NULL,
    exercise_name_snapshot VARCHAR(150) NOT NULL,
    activity_type_snapshot VARCHAR(50) NOT NULL,
    order_index_snapshot INTEGER NOT NULL CHECK (order_index_snapshot > 0),
    planned_sets_snapshot INTEGER CHECK (planned_sets_snapshot > 0),
    planned_rep_range_min_snapshot INTEGER CHECK (planned_rep_range_min_snapshot > 0),
    planned_rep_range_max_snapshot INTEGER CHECK (planned_rep_range_max_snapshot > 0),
    planned_target_load_kg_snapshot NUMERIC(8,2) CHECK (planned_target_load_kg_snapshot >= 0),
    planned_target_load_text_snapshot VARCHAR(100),
    planned_duration_minutes_snapshot INTEGER CHECK (planned_duration_minutes_snapshot > 0),
    planned_distance_km_snapshot NUMERIC(8,2) CHECK (planned_distance_km_snapshot >= 0),
    planned_intensity_text_snapshot VARCHAR(100),
    planned_rest_seconds_snapshot INTEGER CHECK (planned_rest_seconds_snapshot > 0),
    planned_notes_snapshot TEXT,
    actual_duration_minutes INTEGER CHECK (actual_duration_minutes > 0),
    actual_distance_km NUMERIC(8,2) CHECK (actual_distance_km >= 0),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    CONSTRAINT uk_workout_session_activities_order UNIQUE (workout_session_id, order_index_snapshot),
    CONSTRAINT ck_workout_session_activities_rep_range CHECK (
        planned_rep_range_min_snapshot <= planned_rep_range_max_snapshot
    )
);

CREATE INDEX idx_workout_session_activities_origin ON workout_session_activities (workout_activity_id);
CREATE INDEX idx_workout_session_activities_exercise ON workout_session_activities (exercise_id);
