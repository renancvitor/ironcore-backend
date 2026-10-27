package com.ironcore.application.workoutsession.workoutsession.start;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

public record StartWorkoutSessionCommand(
        UserId actorUserId,
        WorkoutCycleId workoutCycleId,
        WorkoutDayId workoutDayId
) {
}
