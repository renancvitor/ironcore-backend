package com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.repository;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.repository.WorkoutActivityRepository;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.exercise.repository.ExerciseJpaRepository;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.entity.WorkoutActivityEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.mapper.WorkoutActivityMapper;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity.WorkoutDayEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.repository.WorkoutDayJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WorkoutActivityAdapter implements WorkoutActivityRepository {

    private final WorkoutActivityJpaRepository workoutActivityJpaRepository;
    private final WorkoutDayJpaRepository workoutDayJpaRepository;
    private final ExerciseJpaRepository exerciseJpaRepository;

    @Override
    public WorkoutActivity save(WorkoutActivity workoutActivity) {
        WorkoutDayEntity workoutDayReference;
        ExerciseEntity exerciseReference;

        try {
            workoutDayReference = workoutDayJpaRepository.getReferenceById(workoutActivity.getWorkoutDayId().value());
            exerciseReference = exerciseJpaRepository.getReferenceById(workoutActivity.getExerciseId().value());
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao obter referências para persistência do workout activity.", exception);
        }

        WorkoutActivityEntity entity;
        try {
            entity = Objects.requireNonNull(WorkoutActivityMapper.toEntity(
                    workoutActivity,
                    workoutDayReference,
                    exerciseReference),
                    "Workout activity retornou entidade nula."
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter domínio para entidade.", exception);
        }

        WorkoutActivityEntity savedEntity;
        try {
            savedEntity = Objects.requireNonNull(
                    workoutActivityJpaRepository.save(entity),
                    "Workout activity retornou entidade nula após persistência."
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao persistir entidade.", exception);
        }

        try {
            return WorkoutActivityMapper.toDomain(savedEntity);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter persistido para domain.", exception);
        }
    }

    @Override
    public Optional<WorkoutActivity> findByIdAndPersonId(WorkoutActivityId id, PersonId personId) {
        Optional<WorkoutActivityEntity> entity;

        try {
            Long workoutActivityId = Objects.requireNonNull(
                    id.value(),
                    "Id da atividade de treino não pode ser nulo."
            );
            Long personIdValue = Objects.requireNonNull(personId.value(), "Id da pessoa não pode ser nulo.");
            entity = workoutActivityJpaRepository.findByIdAndWorkoutDay_WorkoutCycle_Person_Id(
                    workoutActivityId,
                    personIdValue
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar workout activity por id.", exception);
        }

        try {
            return entity.map(WorkoutActivityMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter workout activity por id para domínio.", exception);
        }
    }

    @Override
    public List<WorkoutActivity> findByPersonIdAndWorkoutDayId(PersonId personId, WorkoutDayId workoutDayId) {
        List<WorkoutActivityEntity> entities;

        try {
            Long personIdValue = Objects.requireNonNull(personId.value(), "Id da pessoa não pode ser nulo.");
            Long workoutDayIdValue = Objects.requireNonNull(
                    workoutDayId.value(),
                    "Id do dia de treino não pode ser nulo."
            );
            entities = workoutActivityJpaRepository
                    .findByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdOrderByOrderIndexAsc(
                            personIdValue,
                            workoutDayIdValue
                    );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar workout activities por person id e workout day id.", exception);
        }

        try {
            return entities.stream().map(WorkoutActivityMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException(
                    "Falha ao converter workout activities por person id e workout day id para domínio.",
                    exception
            );
        }
    }

    @Override
    public Boolean existsByPersonIdAndWorkoutDayIdAndExerciseId(
            PersonId personId,
            WorkoutDayId workoutDayId,
            ExerciseId exerciseId
    ) {
        try {
            Long personIdValue = Objects.requireNonNull(personId.value(), "Id da pessoa não pode ser nulo.");
            Long workoutDayIdValue = Objects.requireNonNull(
                    workoutDayId.value(),
                    "Id do dia de treino não pode ser nulo."
            );
            Long exerciseIdValue = Objects.requireNonNull(
                    exerciseId.value(),
                    "Id do exercício não pode ser nulo."
            );

            return workoutActivityJpaRepository.existsByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdAndExercise_Id(
                    personIdValue,
                    workoutDayIdValue,
                    exerciseIdValue
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException(
                    "Falha ao verificar existência de workout activity por person id, workout day id e exercise id.",
                    exception
            );
        }
    }
}
