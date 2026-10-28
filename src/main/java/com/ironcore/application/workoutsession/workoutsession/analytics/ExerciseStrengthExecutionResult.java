package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExerciseStrengthExecutionResult(
        Long sessionId,
        LocalDate performedOn,
        Instant startedAt,
        String exerciseNameSnapshot,
        Long setId,
        BigDecimal loadKg,
        ActualLoadMode mode,
        Integer reps
) {
}
