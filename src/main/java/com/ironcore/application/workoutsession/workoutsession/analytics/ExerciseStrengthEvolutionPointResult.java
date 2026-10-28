package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.workoutsession.analytics.valueobject.StrengthExecutionMetrics;

import java.time.Instant;
import java.time.LocalDate;

public record ExerciseStrengthEvolutionPointResult(
        Long sessionId,
        LocalDate performedOn,
        Instant startedAt,
        String exerciseNameSnapshot,
        StrengthExecutionMetrics metrics
) {
}
