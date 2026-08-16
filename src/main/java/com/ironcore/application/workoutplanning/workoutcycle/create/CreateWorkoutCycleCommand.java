package com.ironcore.application.workoutplanning.workoutcycle.create;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;

public record CreateWorkoutCycleCommand(
        UserId actorUserId,
        String name,
        TrainingGoalId trainingGoalId,
        Integer desiredDurationMonths,
        String notes
) {
}
