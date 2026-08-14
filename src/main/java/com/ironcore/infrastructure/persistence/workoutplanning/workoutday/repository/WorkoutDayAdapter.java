package com.ironcore.infrastructure.persistence.workoutplanning.workoutday.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.repository.WorkoutDayRepository;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.repository.WorkoutCycleJpaRepository;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity.WorkoutDayEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.mapper.WorkoutDayMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class WorkoutDayAdapter implements WorkoutDayRepository {

    private final WorkoutDayJpaRepository workoutDayJpaRepository;
    private final WorkoutCycleJpaRepository workoutCycleJpaRepository;

    @Override
    public WorkoutDay save(WorkoutDay workoutDay) {
        WorkoutCycleEntity workoutCycleReference;

        try {
            workoutCycleReference = workoutCycleJpaRepository.getReferenceById(workoutDay.getWorkoutCycleId().value());
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao obter referência para persistência do workout day.", exception);
        }

        WorkoutDayEntity entity;
        try {
            entity = Objects.requireNonNull(WorkoutDayMapper.toEntity(
                    workoutDay,
                    workoutCycleReference),
                    "Workout day retornou entidade nula."
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter domínio para entidade.", exception);
        }

        WorkoutDayEntity savedEntity;
        try {
            savedEntity = Objects.requireNonNull(
                    workoutDayJpaRepository.save(entity),
                    "Workout day retornou entidade nula após persistência."
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao persistir entidade.", exception);
        }

        try {
            return WorkoutDayMapper.toDomain(savedEntity);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter persistido para domain.", exception);
        }
    }

    @Override
    public Optional<WorkoutDay> findByIdAndPersonId(WorkoutDayId id, PersonId personId) {
        Optional<WorkoutDayEntity> entity;

        try {
            Long workoutDayId = Objects.requireNonNull(id.value(), "Id do dia de treino não pode ser nulo.");
            Long personIdValue = Objects.requireNonNull(personId.value(), "Id da pessoa não pode ser nulo.");

            entity = workoutDayJpaRepository.findByIdAndWorkoutCycle_Person_Id(workoutDayId, personIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar workout day por id.", exception);
        }

        try {
            return entity.map(WorkoutDayMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter workout day por id para domínio.", exception);
        }
    }

    @Override
    public List<WorkoutDay> findByWorkoutCycleId(WorkoutCycleId workoutCycleId) {
        List<WorkoutDayEntity> entities;

        try {
            Long workoutCycleIdValue = Objects.requireNonNull(
                    workoutCycleId.value(),
                    "Id do ciclo de treino não pode ser nulo."
            );

            entities = workoutDayJpaRepository.findByWorkoutCycle_IdOrderByOrderIndexAsc(workoutCycleIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar workout days por workout cycle id.", exception);
        }

        try {
            return entities.stream().map(WorkoutDayMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter workout days por workout cycle id para domínio.", exception);
        }
    }
}
