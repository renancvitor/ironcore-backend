package com.ironcore.domain.workoutplanning.workoutcycle;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class WorkoutCycleTestFactory {

    public static final WorkoutCycleId WORKOUT_CYCLE_ID = new WorkoutCycleId(1L);
    public static final PersonId PERSON_ID = new PersonId(1L);
    public static final TrainingGoalId TRAINING_GOAL_ID = new TrainingGoalId(1L);
    public static final LocalDate START_DATE = LocalDate.of(2026, 1, 1);
    public static final LocalDate END_DATE = LocalDate.of(2026, 3, 31);
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 10, 0);
    public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 1, 1, 11, 0);

    private WorkoutCycleTestFactory() {
    }

    public static WorkoutCycle workoutCycleWithoutId() {
        return WorkoutCycle.register(
                PERSON_ID,
                "Ciclo de hipertrofia",
                TRAINING_GOAL_ID,
                3,
                WorkoutOrigin.MANUAL,
                "Planejamento inicial.",
                CREATED_AT
        );
    }

    public static WorkoutCycle restoredWorkoutCycle(
            WorkoutStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return WorkoutCycle.restore(
                WORKOUT_CYCLE_ID,
                PERSON_ID,
                "Ciclo de hipertrofia",
                TRAINING_GOAL_ID,
                startDate,
                endDate,
                3,
                status,
                WorkoutOrigin.MANUAL,
                "Planejamento restaurado.",
                CREATED_AT,
                UPDATED_AT
        );
    }

    public static WorkoutCycle inProgressWorkoutCycle() {
        return restoredWorkoutCycle(WorkoutStatus.IN_PROGRESS, START_DATE, null);
    }

    public static WorkoutCycle completedWorkoutCycle() {
        return restoredWorkoutCycle(WorkoutStatus.COMPLETED, START_DATE, END_DATE);
    }

    public static WorkoutCycle cancelledWorkoutCycle() {
        return restoredWorkoutCycle(WorkoutStatus.CANCELLED, null, null);
    }
}
