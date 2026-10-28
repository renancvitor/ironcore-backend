package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseCardioExecutionResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseStrengthExecutionResult;
import com.ironcore.application.workoutsession.workoutsession.port.ExerciseAnalyticsQueryPort;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExerciseAnalyticsQueryAdapter implements ExerciseAnalyticsQueryPort {

    private final ExerciseAnalyticsJpaRepository repository;

    @Override
    public List<ExerciseStrengthExecutionResult> findStrengthExecution(
            PersonId personId,
            ExerciseId exerciseId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<ExerciseStrengthExecutionProjection> projections;

        try {
            projections = repository.findStrengthExecution(personId.value(), exerciseId.value(), startDate, endDate);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao consultar evolução de força.", exception);
        }

        try {
            return projections.stream()
                    .map(projection -> new ExerciseStrengthExecutionResult(
                            new WorkoutSessionId(projection.sessionId()),
                            projection.performedOn(),
                            projection.startedAt(),
                            projection.exerciseNameSnapshot(),
                            projection.setId() == null ? null : new WorkoutSessionSetId(projection.setId()),
                            projection.loadKg(),
                            projection.mode(),
                            projection.reps(),
                            projection.workoutDayTitleSnapshot()))
                    .toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao projetar evolução de força.", exception);
        }
    }

    @Override
    public List<ExerciseCardioExecutionResult> findCardioExecution(
            PersonId personId,
            ExerciseId exerciseId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<ExerciseCardioExecutionProjection> projections;

        try {
            projections = repository.findCardioExecution(personId.value(), exerciseId.value(), startDate, endDate);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao consultar evolução de cardio.", exception);
        }

        try {
            return projections.stream()
                    .map(projection -> new ExerciseCardioExecutionResult(
                            new WorkoutSessionId(projection.sessionId()),
                            projection.performedOn(),
                            projection.startedAt(),
                            projection.exerciseNameSnapshot(),
                            projection.durationMinutes(),
                            projection.distanceKm(),
                            projection.workoutDayTitleSnapshot()))
                    .toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao projetar evolução de cardio.", exception);
        }
    }
}
