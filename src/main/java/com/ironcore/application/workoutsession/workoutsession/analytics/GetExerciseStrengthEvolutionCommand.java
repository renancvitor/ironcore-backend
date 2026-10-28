package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.user.valueobject.UserId;

import java.time.LocalDate;

public record GetExerciseStrengthEvolutionCommand(
        UserId actorUserId,
        ExerciseId exerciseId,
        LocalDate startDate,
        LocalDate endDate
) {
}
