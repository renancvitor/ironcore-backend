package com.ironcore.infrastructure.persistence.exercise.repository;

import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.repository.ExerciseRepository;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.exercise.mapper.ExerciseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
