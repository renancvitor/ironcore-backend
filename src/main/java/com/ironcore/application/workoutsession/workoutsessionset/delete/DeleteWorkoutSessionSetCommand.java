package com.ironcore.application.workoutsession.workoutsessionset.delete;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;

public record DeleteWorkoutSessionSetCommand(
        UserId actorUserId,
        WorkoutSessionId id,
        WorkoutSessionActivityId activityId,
        WorkoutSessionSetId setId
) {
}
