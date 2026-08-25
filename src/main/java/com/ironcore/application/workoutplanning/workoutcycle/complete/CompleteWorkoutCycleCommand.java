package com.ironcore.application.workoutplanning.workoutcycle.complete;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

public record CompleteWorkoutCycleCommand(
        UserId actorUserId,
        WorkoutCycleId id
) {
}
