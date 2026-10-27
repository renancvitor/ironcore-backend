package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseAnalyticsQueryPort;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
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
    public List<StrengthRow> strength(PersonId personId, ExerciseId exerciseId, LocalDate startDate, LocalDate endDate) {
        try {
            return repository.strength(personId.value(), exerciseId.value(), startDate, endDate);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao consultar evolução de força.", exception);
        }
    }

    @Override
    public List<CardioRow> cardio(PersonId personId, ExerciseId exerciseId, LocalDate startDate, LocalDate endDate) {
        try {
            return repository.cardio(personId.value(), exerciseId.value(), startDate, endDate);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao consultar evolução de cardio.", exception);
        }
    }
}
