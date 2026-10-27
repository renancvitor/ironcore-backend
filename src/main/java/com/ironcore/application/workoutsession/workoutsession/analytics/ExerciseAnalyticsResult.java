package com.ironcore.application.workoutsession.workoutsession.analytics;

import com.ironcore.domain.workoutsession.analytics.ExecutionMetrics;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ExerciseAnalyticsResult {
    private ExerciseAnalyticsResult() {}
    public record Strength(Long exerciseId, List<StrengthPoint> points) {}
    public record Cardio(Long exerciseId, List<CardioPoint> points, ExecutionMetrics.Cardio summary) {}
    public record StrengthPoint(Long sessionId, LocalDate performedOn, Instant startedAt,
                                String exerciseNameSnapshot, ExecutionMetrics.Strength metrics) {}
    public record CardioPoint(Long sessionId, LocalDate performedOn, Instant startedAt,
                              String exerciseNameSnapshot, ExecutionMetrics.Cardio metrics) {}
}
