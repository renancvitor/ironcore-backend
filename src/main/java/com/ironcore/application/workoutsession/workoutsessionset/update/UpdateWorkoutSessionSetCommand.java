package com.ironcore.application.workoutsession.workoutsessionset.update;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;

import java.math.BigDecimal;

public record UpdateWorkoutSessionSetCommand(
        UserId actorUserId,
        WorkoutSessionId id,
        WorkoutSessionActivityId activityId,
        WorkoutSessionSetId setId,
        BigDecimal actualLoadKg, boolean actualLoadKgPresent, ActualLoadMode actualLoadMode, boolean actualLoadModePresent,
        Integer actualReps, boolean actualRepsPresent, String notes, boolean notesPresent
) {
}
