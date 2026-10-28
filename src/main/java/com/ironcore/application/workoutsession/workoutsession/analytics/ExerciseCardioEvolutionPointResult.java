package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.workoutsession.analytics.valueobject.CardioExecutionMetrics;

import java.time.Instant;
import java.time.LocalDate;

public record ExerciseCardioEvolutionPointResult(
        Long sessionId,
        LocalDate performedOn,
        Instant startedAt,
        String exerciseNameSnapshot,
        CardioExecutionMetrics metrics
) {
}
