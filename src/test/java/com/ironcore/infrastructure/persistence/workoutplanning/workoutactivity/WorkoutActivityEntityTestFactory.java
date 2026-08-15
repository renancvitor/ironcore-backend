package com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity;

import com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.entity.WorkoutActivityEntity;

import java.math.BigDecimal;

import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.CREATED_AT;
import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.UPDATED_AT;
import static com.ironcore.infrastructure.persistence.exercise.ExerciseEntityTestFactory.exerciseEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutday.WorkoutDayEntityTestFactory.workoutDayEntity;

public final class WorkoutActivityEntityTestFactory {

    private WorkoutActivityEntityTestFactory() {
    }

    public static WorkoutActivityEntity workoutActivityEntity() {
        return workoutActivityEntity(1L);
    }

    public static WorkoutActivityEntity invalidWorkoutActivityEntity() {
        return workoutActivityEntity(null);
    }

    private static WorkoutActivityEntity workoutActivityEntity(Long id) {
        return new WorkoutActivityEntity(
                id,
                workoutDayEntity(),
                exerciseEntity(),
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
