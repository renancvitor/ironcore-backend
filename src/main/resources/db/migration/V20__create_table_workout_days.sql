CREATE TABLE workout_days(
    id BIGSERIAL PRIMARY KEY,
    workout_cycle_id BIGINT NOT NULL,
    week_day INTEGER NOT NULL,
    title VARCHAR(100) NOT NULL,
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    CONSTRAINT fk_workout_day_workout_cycle FOREIGN KEY (workout_cycle_id) REFERENCES workout_cycles(id),

    CONSTRAINT uk_workout_days_association UNIQUE (workout_cycle_id, week_day, sort_order) DEFERRABLE INITIALLY DEFERRED,

    CONSTRAINT ck_workout_days_week_day CHECK (week_day BETWEEN 1 AND 7),

    CONSTRAINT ck_workout_days_sort_order_positive CHECK (sort_order > 0)
);