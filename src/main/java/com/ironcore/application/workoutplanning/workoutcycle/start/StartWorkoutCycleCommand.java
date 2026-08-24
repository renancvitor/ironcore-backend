package com.ironcore.application.workoutplanning.workoutcycle.start;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

public record StartWorkoutCycleCommand(
        UserId actorUserId,
        WorkoutCycleId id
) {
}
