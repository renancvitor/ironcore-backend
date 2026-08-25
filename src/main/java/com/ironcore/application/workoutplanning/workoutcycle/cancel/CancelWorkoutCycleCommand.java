package com.ironcore.application.workoutplanning.workoutcycle.cancel;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

public record CancelWorkoutCycleCommand(
        UserId actorUserId,
        WorkoutCycleId id
) {
}
