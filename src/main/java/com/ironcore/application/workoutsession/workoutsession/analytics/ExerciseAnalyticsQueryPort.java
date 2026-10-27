package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface ExerciseAnalyticsQueryPort {
    List<StrengthRow> strength(PersonId personId, ExerciseId exerciseId, LocalDate startDate, LocalDate endDate);
    List<CardioRow> cardio(PersonId personId, ExerciseId exerciseId, LocalDate startDate, LocalDate endDate);

    record StrengthRow(Long sessionId, LocalDate performedOn, Instant startedAt, String exerciseNameSnapshot,
                       Long setId, BigDecimal loadKg, ActualLoadMode mode, Integer reps) {}
    record CardioRow(Long sessionId, LocalDate performedOn, Instant startedAt, String exerciseNameSnapshot,
                     Integer durationMinutes, BigDecimal distanceKm) {}
}
