package com.ironcore.domain.workoutplanning.workoutactivity;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class WorkoutActivityTestFactory {

    public static final WorkoutActivityId WORKOUT_ACTIVITY_ID = new WorkoutActivityId(1L);
    public static final WorkoutDayId WORKOUT_DAY_ID = new WorkoutDayId(1L);
    public static final ExerciseId EXERCISE_ID = new ExerciseId(1L);
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 10, 0);
    public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 1, 1, 11, 0);

    private WorkoutActivityTestFactory() {
    }

    public static WorkoutActivity workoutActivityWithoutId() {
        return WorkoutActivity.register(
                WORKOUT_DAY_ID,
                EXERCISE_ID,
                1,
                4,
                8,
                12,
                new BigDecimal("80.50"),
                "RPE 8",
                45,
                new BigDecimal("5.50"),
                "Moderada",
                90,
                "Priorizar a técnica",
                CREATED_AT
        );
    }

    public static WorkoutActivity restoredWorkoutActivity() {
        return WorkoutActivity.restore(
                WORKOUT_ACTIVITY_ID,
                WORKOUT_DAY_ID,
                EXERCISE_ID,
                2,
                5,
                6,
                10,
                new BigDecimal("90.00"),
                "RPE 9",
                50,
                new BigDecimal("6.00"),
                "Alta",
                120,
                "Manter cadência controlada",
                CREATED_AT,
                UPDATED_AT
        );
    }
}
