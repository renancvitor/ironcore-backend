package com.ironcore.application.workoutsession.workoutsession.analytics;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExerciseCardioExecutionResult(
        Long sessionId,
        LocalDate performedOn,
        Instant startedAt,
        String exerciseNameSnapshot,
        Integer durationMinutes,
        BigDecimal distanceKm
) {
}
