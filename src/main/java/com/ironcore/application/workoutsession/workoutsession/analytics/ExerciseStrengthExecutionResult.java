package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExerciseStrengthExecutionResult(
        WorkoutSessionId sessionId,
        LocalDate performedOn,
        Instant startedAt,
        String exerciseNameSnapshot,
        WorkoutSessionSetId setId,
        BigDecimal loadKg,
        ActualLoadMode mode,
        Integer reps
) {
}
