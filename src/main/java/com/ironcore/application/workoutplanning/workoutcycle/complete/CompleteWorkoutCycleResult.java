package com.ironcore.application.workoutplanning.workoutcycle.complete;

import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

import java.time.LocalDate;

public record CompleteWorkoutCycleResult(
        WorkoutCycleId id,
        TrainingGoalId trainingGoalId,
        LocalDate startDate,
        LocalDate endDate,
        WorkoutStatus workoutStatus
) {
}
