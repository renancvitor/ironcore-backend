ALTER TABLE workout_session_sets
    DROP CONSTRAINT ck_workout_session_sets_execution;

ALTER TABLE workout_session_sets
    ADD CONSTRAINT ck_workout_session_sets_execution CHECK (
        ((actual_load_kg IS NULL AND actual_load_mode IS NULL)
            OR (actual_load_kg IS NOT NULL AND actual_load_mode IS NOT NULL))
        AND (actual_load_kg IS NOT NULL OR actual_reps IS NOT NULL
            OR (notes IS NOT NULL AND LENGTH(TRIM(notes)) > 0))
    );
