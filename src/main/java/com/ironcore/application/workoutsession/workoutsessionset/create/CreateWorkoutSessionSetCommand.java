package com.ironcore.application.workoutsession.workoutsessionset.create;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;

import java.math.BigDecimal;

public record CreateWorkoutSessionSetCommand(
        UserId actorUserId,
        WorkoutSessionId id,
        WorkoutSessionActivityId activityId,
        Integer setNumber, BigDecimal actualLoadKg, ActualLoadMode actualLoadMode,
        Integer actualReps, String notes
) {
}
