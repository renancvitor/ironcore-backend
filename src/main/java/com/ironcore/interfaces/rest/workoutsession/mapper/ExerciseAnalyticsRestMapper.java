package com.ironcore.interfaces.rest.workoutsession.mapper;

import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseCardioEvolutionPointResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseStrengthEvolutionPointResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseCardioEvolutionCommand;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseCardioEvolutionResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseStrengthEvolutionCommand;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseStrengthEvolutionResult;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutsession.analytics.valueobject.CardioExecutionMetrics;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutsession.dto.analytics.ExerciseCardioEvolutionPointResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.analytics.ExerciseCardioEvolutionResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.analytics.ExerciseCardioEvolutionSummaryResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.analytics.ExerciseStrengthEvolutionPointResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.analytics.ExerciseStrengthEvolutionResponse;

import java.time.LocalDate;

public final class ExerciseAnalyticsRestMapper {

    private ExerciseAnalyticsRestMapper() {
    }

    public static GetExerciseStrengthEvolutionCommand toStrengthEvolutionCommand(
            AuthenticatedUser authenticatedUser,
            Long exerciseId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new GetExerciseStrengthEvolutionCommand(
                authenticatedUser.userId(), new ExerciseId(exerciseId), startDate, endDate
        );
    }

    public static ExerciseStrengthEvolutionResponse toResponse(GetExerciseStrengthEvolutionResult result) {
        return new ExerciseStrengthEvolutionResponse(
                result.exerciseId().value(),
                result.points().stream().map(ExerciseAnalyticsRestMapper::toPointResponse).toList()
        );
    }

    private static ExerciseStrengthEvolutionPointResponse toPointResponse(ExerciseStrengthEvolutionPointResult point) {
        var metrics = point.metrics();

        return new ExerciseStrengthEvolutionPointResponse(
                point.sessionId().value(),
                point.performedOn(),
                point.startedAt(),
                point.exerciseNameSnapshot(),
                metrics.maxNormalizedLoadKg(),
                metrics.volumeKgReps(),
                metrics.registeredSetCount(),
                metrics.loadedSetCount(),
                metrics.volumeSetCount()
        );
    }

    public static GetExerciseCardioEvolutionCommand toCardioEvolutionCommand(
            AuthenticatedUser authenticatedUser,
            Long exerciseId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new GetExerciseCardioEvolutionCommand(
                authenticatedUser.userId(), new ExerciseId(exerciseId), startDate, endDate
        );
    }

    public static ExerciseCardioEvolutionResponse toResponse(GetExerciseCardioEvolutionResult result) {
        return new ExerciseCardioEvolutionResponse(
                result.exerciseId().value(),
                result.points().stream().map(ExerciseAnalyticsRestMapper::toPointResponse).toList(),
                toSummaryResponse(result.summary())
        );
    }

    private static ExerciseCardioEvolutionPointResponse toPointResponse(ExerciseCardioEvolutionPointResult point) {
        var metrics = point.metrics();

        return new ExerciseCardioEvolutionPointResponse(
                point.sessionId().value(),
                point.performedOn(),
                point.startedAt(),
                point.exerciseNameSnapshot(),
                metrics.distanceKm(),
                metrics.durationMinutes(),
                metrics.averagePaceMinPerKm(),
                metrics.averageSpeedKmPerHour(),
                metrics.activityCount(),
                metrics.distanceRecordCount(),
                metrics.durationRecordCount(),
                metrics.paceRecordCount(),
                metrics.speedRecordCount()
        );
    }

    private static ExerciseCardioEvolutionSummaryResponse toSummaryResponse(CardioExecutionMetrics metrics) {
        return new ExerciseCardioEvolutionSummaryResponse(
                metrics.distanceKm(),
                metrics.durationMinutes(),
                metrics.averagePaceMinPerKm(),
                metrics.averageSpeedKmPerHour(),
                metrics.distanceRecordCount(),
                metrics.durationRecordCount(),
                metrics.paceRecordCount(),
                metrics.speedRecordCount()
        );
    }
}
