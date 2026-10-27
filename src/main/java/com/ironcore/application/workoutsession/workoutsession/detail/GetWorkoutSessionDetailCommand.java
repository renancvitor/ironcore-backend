package com.ironcore.application.workoutsession.workoutsession.detail;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;

public record GetWorkoutSessionDetailCommand(
        UserId actorUserId,
        WorkoutSessionId id
) {
}
