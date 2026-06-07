CREATE TABLE user_body_metrics(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    measured_at TIMESTAMP NOT NULL,
    weight_kg DOUBLE PRECISION NOT NULL,
    height_cm DOUBLE PRECISION NOT NULL,
    neck_cm DOUBLE PRECISION,
    chest_cm DOUBLE PRECISION,
    shoulder_cm DOUBLE PRECISION,
    arm_cm DOUBLE PRECISION,
    forearm_cm DOUBLE PRECISION,
    waist_cm DOUBLE PRECISION,
    hip_cm DOUBLE PRECISION,
    thigh_cm DOUBLE PRECISION,
    calf_cm DOUBLE PRECISION,
    bmi DOUBLE PRECISION,
    body_fat_percentage DOUBLE PRECISION,
    fat_mass_kg DOUBLE PRECISION,
    lean_mass_kg DOUBLE PRECISION,
    updated_at TIMESTAMP,
    notes TEXT,

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);