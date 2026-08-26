package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.entity.TrainingGoalEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.repository.TrainingGoalJpaRepository;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.mapper.WorkoutCycleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WorkoutCycleAdapter implements WorkoutCycleRepository {

    private final WorkoutCycleJpaRepository workoutCycleJpaRepository;
    private final PersonJpaRepository personJpaRepository;
    private final TrainingGoalJpaRepository trainingGoalJpaRepository;

    @Override
    public WorkoutCycle save(WorkoutCycle workoutCycle) {
        PersonEntity personReference;
        TrainingGoalEntity trainingGoalReference;

        try {
            personReference = personJpaRepository.getReferenceById(workoutCycle.getPersonId().value());

            trainingGoalReference = trainingGoalJpaRepository.getReferenceById(
                    workoutCycle.getTrainingGoalId().value()
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao obter referências para persistência do workout cycle.", exception);
        }

        WorkoutCycleEntity entity;
        try {
            entity = Objects.requireNonNull(WorkoutCycleMapper.toEntity(
                            workoutCycle,
                            personReference,
                            trainingGoalReference),
                    "Workout cycle retornou entidade nula."
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter domínio para entidade.", exception);
        }

        WorkoutCycleEntity savedEntity;
        try {
            savedEntity =  Objects.requireNonNull(
                    workoutCycleJpaRepository.save(entity),
                    "Workout cycle retornou entidade nula após persistência."
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao persistir entidade.", exception);
        }

        try {
            return WorkoutCycleMapper.toDomain(savedEntity);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter persistido para domain.", exception);
        }
    }

    @Override
    public Optional<WorkoutCycle> findByIdAndPersonId(WorkoutCycleId id, PersonId personId) {
        Optional<WorkoutCycleEntity> entity;

        try {
            Long workoutCycleId = Objects.requireNonNull(id.value(), "Id do ciclo de treino não pode ser nulo.");
            Long personIdValue = Objects.requireNonNull(personId.value(), "Id da pessoa não pode ser nulo.");
            entity = workoutCycleJpaRepository.findByIdAndPerson_Id(workoutCycleId, personIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar workout cycle por id.", exception);
        }

        try {
            return entity.map(WorkoutCycleMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter workout cycle por id para domínio.", exception);
        }
    }

    @Override
    public List<WorkoutCycle> findByPersonId(PersonId personId) {
        List<WorkoutCycleEntity> entities;

        try {
            Long personIdValue = Objects.requireNonNull(personId.value(), "Id da pessoa não pode ser nulo.");
            entities = workoutCycleJpaRepository.findByPerson_Id(personIdValue);
        }  catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar workout cycle por person id.", exception);
        }

        try {
            return entities.stream().map(WorkoutCycleMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter workout cycle por person id para domínio.", exception);
        }
    }

    @Override
    public List<WorkoutCycle> findByPersonIdAndWorkoutStatus(PersonId personId, WorkoutStatus workoutStatus) {
        List<WorkoutCycleEntity> entities;

        try {
            Long personIdValue = Objects.requireNonNull(personId.value(), "Id da pessoa não pode ser nulo.");
            entities = workoutCycleJpaRepository.findByPerson_IdAndWorkoutStatus(personIdValue, workoutStatus);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar workout cycle por person id e workout status.", exception);
        }

        try {
            return entities.stream().map(WorkoutCycleMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException(
                    "Falha ao converter workout cycle por person id e workout status para domínio.",
                    exception
            );
        }
    }

    @Override
    public List<WorkoutCycle> findByPersonIdAndTrainingGoalId(PersonId personId, TrainingGoalId trainingGoalId) {
        List<WorkoutCycleEntity> entities;

        try {
            Long personIdValue = Objects.requireNonNull(personId.value(), "Id da pessoa não pode ser nulo.");
            Long trainingGoalIdValue = Objects.requireNonNull(
                    trainingGoalId.value(),
                    "Id do objetivo do treino não pode ser nulo."
            );
            entities = workoutCycleJpaRepository.findByPerson_IdAndTrainingGoal_Id(personIdValue, trainingGoalIdValue);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar workout cycle por person id e training goal id.", exception);
        }

        try {
            return entities.stream().map(WorkoutCycleMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException(
                    "Falha ao converter workout cycle por person id e training goal id para domínio.",
                    exception
            );
        }
    }

    @Override
    public void deleteById(WorkoutCycleId id) {
        try {
            Long workoutCycleId = Objects.requireNonNull(id.value(), "Id do ciclo de treino não pode ser nulo.");
            workoutCycleJpaRepository.deleteById(workoutCycleId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao excluir ciclo de treino por id.", exception);
        }
    }
}
