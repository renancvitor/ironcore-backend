package com.ironcore.application.workoutplanning.workoutcycle.detail;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

public record GetWorkoutCycleDetailCommand(
        UserId actoruserId,
        WorkoutCycleId id
) {
}
