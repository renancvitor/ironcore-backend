CREATE TABLE workout_sessions (
    id BIGSERIAL PRIMARY KEY,
    person_id BIGINT NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    workout_cycle_id BIGINT REFERENCES workout_cycles(id) ON DELETE SET NULL,
    workout_day_id BIGINT REFERENCES workout_days(id) ON DELETE SET NULL,
    workout_cycle_name_snapshot VARCHAR(150) NOT NULL,
    workout_day_title_snapshot VARCHAR(100) NOT NULL,
    week_day_snapshot SMALLINT NOT NULL CHECK (week_day_snapshot BETWEEN 1 AND 7),
    performed_on DATE NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL CHECK (status IN ('IN_PROGRESS', 'CLOSED')),
    close_reason VARCHAR(30) CHECK (close_reason IN ('MANUAL', 'AUTO_EXPIRED')),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_workout_sessions_lifecycle CHECK (
        (status = 'IN_PROGRESS' AND finished_at IS NULL AND close_reason IS NULL)
        OR (status = 'CLOSED' AND finished_at IS NOT NULL AND close_reason IS NOT NULL)
    ),
    CONSTRAINT ck_workout_sessions_dates CHECK (
        performed_on = (started_at AT TIME ZONE 'America/Sao_Paulo')::DATE
        AND (finished_at IS NULL OR (finished_at >= started_at AND
            finished_at <= ((performed_on + 1) + TIME '03:00') AT TIME ZONE 'America/Sao_Paulo'))
        AND (close_reason IS DISTINCT FROM 'AUTO_EXPIRED' OR
            finished_at = ((performed_on + 1) + TIME '03:00') AT TIME ZONE 'America/Sao_Paulo')
        AND (updated_at IS NULL OR updated_at >= COALESCE(finished_at, created_at))
    )
);

CREATE UNIQUE INDEX uk_workout_sessions_person_in_progress ON workout_sessions (person_id) WHERE status = 'IN_PROGRESS';
CREATE INDEX idx_workout_sessions_person ON workout_sessions (person_id);
CREATE INDEX idx_workout_sessions_cycle ON workout_sessions (workout_cycle_id);
CREATE INDEX idx_workout_sessions_day ON workout_sessions (workout_day_id);
