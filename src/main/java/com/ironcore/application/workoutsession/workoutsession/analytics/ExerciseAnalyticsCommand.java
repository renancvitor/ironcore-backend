package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import java.time.LocalDate;

public record ExerciseAnalyticsCommand(
        UserId actorUserId,
        ExerciseId exerciseId,
        LocalDate startDate,
        LocalDate endDate
) {}
