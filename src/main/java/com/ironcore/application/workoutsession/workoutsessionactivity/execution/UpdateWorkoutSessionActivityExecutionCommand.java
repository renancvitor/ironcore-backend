package com.ironcore.application.workoutsession.workoutsessionactivity.execution;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;

import java.math.BigDecimal;

public record UpdateWorkoutSessionActivityExecutionCommand(
        UserId actorUserId,
        WorkoutSessionId id,
        WorkoutSessionActivityId activityId,
        Integer actualDurationMinutes, boolean actualDurationMinutesPresent,
        BigDecimal actualDistanceKm, boolean actualDistanceKmPresent, String notes, boolean notesPresent
) {
}
