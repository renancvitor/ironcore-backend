package com.ironcore.infrastructure.persistence.exercise.repository;

import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.repository.ExerciseRepository;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.exercise.mapper.ExerciseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExerciseAdapter implements ExerciseRepository {

    private final ExerciseJpaRepository exerciseJpaRepository;

    @Override
    public Optional<Exercise> findById(ExerciseId id) {
        Optional<ExerciseEntity> entity;
        try {
            Long exerciseId = Objects.requireNonNull(id.value(), "Id do exercício não pode ser nulo.");
            entity = exerciseJpaRepository.findById(exerciseId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar exercise por id.", exception);
        }

        try {
            return entity.map(ExerciseMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter exercício de entidade para domínio.", exception);
        }
    }

    @Override
    public List<Exercise> findByEquipmentTypeId(EquipmentTypeId id) {
        List<ExerciseEntity> entities;
        try {
            Long equipmentTypeId = Objects.requireNonNull(
                    id.value(),
                    "Id do tipo de equipamento não pode ser nulo."
            );
            entities = exerciseJpaRepository.findByEquipmentType_Id(equipmentTypeId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar exercise por equipment type id.", exception);
        }

        try {
            return entities.stream().map(ExerciseMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter exercício de entidade para domínio.", exception);
        }
    }

    @Override
    public List<Exercise> findByActivityTypeId(ActivityTypeId id) {
        List<ExerciseEntity> entities;
        try {
            Long activityTypeId = Objects.requireNonNull(
                    id.value(),
                    "Id do tipo de atividade não pode ser nulo."
            );
            entities = exerciseJpaRepository.findByActivityType_Id(activityTypeId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar exercise por activity type id.", exception);
        }

        try {
            return entities.stream().map(ExerciseMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter exercício de entidade para domínio.", exception);
        }
    }

    @Override
    public List<Exercise> findAll() {
        List<ExerciseEntity> entities;
        try {
            entities = exerciseJpaRepository.findAll();
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar exercises.", exception);
        }

        try {
            return entities.stream().map(ExerciseMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter exercício de entidade para domínio.", exception);
        }
    }
}
