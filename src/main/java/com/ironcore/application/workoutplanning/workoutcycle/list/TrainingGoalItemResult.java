package com.ironcore.application.workoutplanning.workoutcycle.list;

import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;

public record TrainingGoalItemResult(
        TrainingGoalId id,
        String name
) {
}
