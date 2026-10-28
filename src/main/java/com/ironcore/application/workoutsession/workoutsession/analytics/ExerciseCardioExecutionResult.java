package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExerciseCardioExecutionResult(
        WorkoutSessionId sessionId,
        LocalDate performedOn,
        Instant startedAt,
        String exerciseNameSnapshot,
        Integer durationMinutes,
        BigDecimal distanceKm,
        String workoutDayTitleSnapshot
) {
}
