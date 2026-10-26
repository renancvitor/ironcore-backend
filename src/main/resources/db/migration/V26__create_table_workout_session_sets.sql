CREATE TABLE workout_session_sets (
    id BIGSERIAL PRIMARY KEY,
    workout_session_activity_id BIGINT NOT NULL REFERENCES workout_session_activities(id) ON DELETE CASCADE,
    set_number INTEGER NOT NULL CHECK (set_number > 0),
    actual_load_kg NUMERIC(8,2) CHECK (actual_load_kg >= 0),
    actual_load_mode VARCHAR(20) CHECK (actual_load_mode IN ('TOTAL', 'PER_SIDE')),
    actual_reps INTEGER CHECK (actual_reps > 0),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    CONSTRAINT uk_workout_session_sets_number UNIQUE (workout_session_activity_id, set_number),
    CONSTRAINT ck_workout_session_sets_execution CHECK (
        (actual_load_kg IS NULL AND actual_reps IS NULL AND actual_load_mode IS NULL
            AND notes IS NOT NULL AND LENGTH(TRIM(notes)) > 0)
        OR (actual_load_kg IS NOT NULL AND actual_reps IS NOT NULL AND actual_load_mode IS NOT NULL)
    )
);
