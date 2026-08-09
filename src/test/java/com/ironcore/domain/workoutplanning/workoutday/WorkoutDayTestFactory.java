package com.ironcore.domain.workoutplanning.workoutday;

import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

import java.time.LocalDateTime;

public final class WorkoutDayTestFactory {

    public static final WorkoutDayId WORKOUT_DAY_ID = new WorkoutDayId(1L);
    public static final WorkoutCycleId WORKOUT_CYCLE_ID = new WorkoutCycleId(1L);
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 10, 0);
    public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 1, 1, 11, 0);

    private WorkoutDayTestFactory() {
    }

    public static WorkoutDay workoutDayWithoutId() {
        return WorkoutDay.register(
                WORKOUT_CYCLE_ID,
                WeekDay.MONDAY,
                "Treino de membros superiores",
                1,
                CREATED_AT
        );
    }

    public static WorkoutDay restoredWorkoutDay() {
        return WorkoutDay.restore(
                WORKOUT_DAY_ID,
                WORKOUT_CYCLE_ID,
                WeekDay.WEDNESDAY,
                "Treino de membros inferiores",
                2,
                CREATED_AT,
                UPDATED_AT
        );
    }
}
