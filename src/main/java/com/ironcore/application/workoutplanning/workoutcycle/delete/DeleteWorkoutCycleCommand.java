package com.ironcore.application.workoutplanning.workoutcycle.delete;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

public record DeleteWorkoutCycleCommand(
        UserId actorUserId,
        WorkoutCycleId id
) {
}
