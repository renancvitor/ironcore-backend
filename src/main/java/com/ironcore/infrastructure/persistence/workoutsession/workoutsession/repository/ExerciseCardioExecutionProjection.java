package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExerciseCardioExecutionProjection(
        Long sessionId,
        LocalDate performedOn,
        Instant startedAt,
        String exerciseNameSnapshot,
        Integer durationMinutes,
        BigDecimal distanceKm,
        String workoutDayTitleSnapshot
) {
}
