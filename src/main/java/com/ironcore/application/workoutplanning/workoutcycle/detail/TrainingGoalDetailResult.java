package com.ironcore.application.workoutplanning.workoutcycle.detail;

import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;

public record TrainingGoalDetailResult(
        TrainingGoalId id,
        String name
) {
}
