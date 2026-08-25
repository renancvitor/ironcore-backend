package com.ironcore.application.workoutplanning.workoutcycle.cancel;

import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

public record CancelWorkoutCycleResult(
        WorkoutCycleId id,
        TrainingGoalId trainingGoalId,
        WorkoutStatus workoutStatus
) {
}
