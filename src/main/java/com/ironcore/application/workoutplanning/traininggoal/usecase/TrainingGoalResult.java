package com.ironcore.application.workoutplanning.traininggoal.usecase;

import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalCode;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;

public record TrainingGoalResult(
        TrainingGoalId id,
        TrainingGoalCode code,
        String name
) {
}
