package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExerciseStrengthExecutionProjection(
        Long sessionId,
        LocalDate performedOn,
        Instant startedAt,
        String exerciseNameSnapshot,
        Long setId,
        BigDecimal loadKg,
        ActualLoadMode mode,
        Integer reps,
        String workoutDayTitleSnapshot
) {
}
