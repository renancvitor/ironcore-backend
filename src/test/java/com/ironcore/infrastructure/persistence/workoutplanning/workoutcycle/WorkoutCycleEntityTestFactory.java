package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;

import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.CREATED_AT;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.START_DATE;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.UPDATED_AT;
import static com.ironcore.infrastructure.persistence.person.PersonEntityTestFactory.personEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.TrainingGoalEntityTestFactory.trainingGoalEntity;

public final class WorkoutCycleEntityTestFactory {

    private WorkoutCycleEntityTestFactory() {
    }

    public static WorkoutCycleEntity workoutCycleEntity() {
        return workoutCycleEntity(1L);
    }

    public static WorkoutCycleEntity invalidWorkoutCycleEntity() {
        return workoutCycleEntity(null);
    }

    private static WorkoutCycleEntity workoutCycleEntity(Long id) {
        return new WorkoutCycleEntity(
                id,
                personEntity(),
                "Ciclo de hipertrofia",
                trainingGoalEntity(),
                START_DATE,
                null,
                3,
                WorkoutStatus.IN_PROGRESS,
                WorkoutOrigin.MANUAL,
                "Planejamento restaurado.",
                CREATED_AT,
                UPDATED_AT
        );
    }
}
