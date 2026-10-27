package com.ironcore.application.workoutsession.workoutsessionactivity.loadmode;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;

public record ChangeWorkoutSessionActivityLoadModeCommand(
        UserId actorUserId,
        WorkoutSessionId id,
        WorkoutSessionActivityId activityId,
        ActualLoadMode actualLoadMode
) {
}
