package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.workoutsession.analytics.valueobject.CardioExecutionMetrics;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;

import java.time.Instant;
import java.time.LocalDate;

public record ExerciseCardioEvolutionPointResult(
        WorkoutSessionId sessionId,
        LocalDate performedOn,
        Instant startedAt,
        String exerciseNameSnapshot,
        CardioExecutionMetrics metrics,
        String workoutDayTitleSnapshot
) {
}
