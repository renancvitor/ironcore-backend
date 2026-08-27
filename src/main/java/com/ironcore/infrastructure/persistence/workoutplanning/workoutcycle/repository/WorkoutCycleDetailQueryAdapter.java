package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.repository;

import com.ironcore.application.workoutplanning.workoutcycle.detail.WorkoutCycleDetailProjection;
import com.ironcore.application.workoutplanning.workoutcycle.port.WorkoutCycleDetailQueryPort;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.infrastructure.exception.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class WorkoutCycleDetailQueryAdapter implements WorkoutCycleDetailQueryPort {

    private final WorkoutCycleDetailJpaRepository workoutCycleDetailJpaRepository;

    @Override
    public List<WorkoutCycleDetailProjection> findDetail(WorkoutCycleId workoutCycleId, PersonId personId) {
        try {
            Long workoutCycleIdValue = Objects.requireNonNull(
                    workoutCycleId.value(),
                    "Id do ciclo de treino não pode ser nulo."
            );

            Long personIdValue = Objects.requireNonNull(personId.value(), "Id da pessoa não pode ser nulo.");

            return workoutCycleDetailJpaRepository.findDetail(workoutCycleIdValue, personIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar detalhes do ciclo de treino.", exception);
        }
    }
}
