package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.workoutsession.analytics.valueobject.StrengthExecutionMetrics;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;

import java.time.Instant;
import java.time.LocalDate;

public record ExerciseStrengthEvolutionPointResult(
        WorkoutSessionId sessionId,
        LocalDate performedOn,
        Instant startedAt,
        String exerciseNameSnapshot,
        StrengthExecutionMetrics metrics,
        String workoutDayTitleSnapshot
) {
}
