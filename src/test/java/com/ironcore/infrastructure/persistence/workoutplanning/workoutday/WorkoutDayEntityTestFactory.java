package com.ironcore.infrastructure.persistence.workoutplanning.workoutday;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity.WorkoutDayEntity;

import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.CREATED_AT;
import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.UPDATED_AT;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.WorkoutCycleEntityTestFactory.workoutCycleEntity;

public final class WorkoutDayEntityTestFactory {

    private WorkoutDayEntityTestFactory() {
    }

    public static WorkoutDayEntity workoutDayEntity() {
        return workoutDayEntity(1L);
    }

    public static WorkoutDayEntity invalidWorkoutDayEntity() {
        return workoutDayEntity(null);
    }

    private static WorkoutDayEntity workoutDayEntity(Long id) {
        return new WorkoutDayEntity(
                id,
                workoutCycleEntity(),
                WeekDay.WEDNESDAY,
                "Treino de membros inferiores",
                2,
                CREATED_AT,
                UPDATED_AT
        );
    }
}
