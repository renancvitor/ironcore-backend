package com.ironcore.application.workoutplanning.workoutactivity.delete;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;

public record DeleteWorkoutActivityCommand(
        UserId actorUserId,
        WorkoutActivityId id
) {
}
