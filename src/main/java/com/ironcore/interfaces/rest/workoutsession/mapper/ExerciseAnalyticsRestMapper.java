package com.ironcore.interfaces.rest.workoutsession.mapper;

import com.ironcore.application.workoutsession.workoutsession.analytics.*;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutsession.dto.analytics.*;
import java.time.LocalDate;

public final class ExerciseAnalyticsRestMapper {
    private ExerciseAnalyticsRestMapper() {}
    public static ExerciseAnalyticsCommand command(AuthenticatedUser user, Long exerciseId, LocalDate start, LocalDate end) {
        return new ExerciseAnalyticsCommand(user.userId(), new ExerciseId(exerciseId), start, end);
    }
    public static ExerciseStrengthEvolutionResponse response(ExerciseAnalyticsResult.Strength result) {
        return new ExerciseStrengthEvolutionResponse(result.exerciseId(), result.points().stream().map(p -> {
            var m = p.metrics();
            return new ExerciseStrengthEvolutionResponse.Point(p.sessionId(), p.performedOn(), p.startedAt(),
                    p.exerciseNameSnapshot(), m.maxNormalizedLoadKg(), m.volumeKgReps(),
                    m.registeredSetCount(), m.loadedSetCount(), m.volumeSetCount());
        }).toList());
    }
    public static ExerciseCardioEvolutionResponse response(ExerciseAnalyticsResult.Cardio result) {
        var summary = result.summary();
        return new ExerciseCardioEvolutionResponse(result.exerciseId(), result.points().stream().map(p -> {
            var m = p.metrics();
            return new ExerciseCardioEvolutionResponse.Point(p.sessionId(), p.performedOn(), p.startedAt(),
                    p.exerciseNameSnapshot(), m.distanceKm(), m.durationMinutes(), m.averagePaceMinPerKm(),
                    m.averageSpeedKmPerHour(), m.activityCount(), m.distanceRecordCount(), m.durationRecordCount(),
                    m.paceRecordCount(), m.speedRecordCount());
        }).toList(), new ExerciseCardioEvolutionResponse.Summary(summary.distanceKm(), summary.durationMinutes(),
                summary.averagePaceMinPerKm(), summary.averageSpeedKmPerHour(), summary.distanceRecordCount(),
                summary.durationRecordCount(), summary.paceRecordCount(), summary.speedRecordCount()));
    }
}
