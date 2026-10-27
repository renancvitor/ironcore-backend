package com.ironcore.application.workoutsession.workoutsession.close;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;

public record CloseWorkoutSessionCommand(
        UserId actorUserId,
        WorkoutSessionId id
) {
}
