CREATE TABLE workout_days(
    id BIGSERIAL PRIMARY KEY,
    workout_cycle_id BIGINT NOT NULL,
    week_day VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    CONSTRAINT fk_workout_day_workout_cycle FOREIGN KEY (workout_cycle_id) REFERENCES workout_cycles(id),

    CONSTRAINT uk_workout_days_association UNIQUE (workout_cycle_id, order_index),

    CONSTRAINT ck_workout_days_order_index_positive CHECK (order_index > 0)
);