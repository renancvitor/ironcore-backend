package com.ironcore.infrastructure.persistence.exercisemuscletarget.repository;

import com.ironcore.domain.exercisemuscletarget.model.ExerciseMuscleTarget;
import com.ironcore.domain.exercisemuscletarget.repository.ExerciseMuscleTargetRepository;
import com.ironcore.domain.exercisemuscletarget.valueobject.ExerciseMuscleTargetId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.exercisemuscletarget.entity.ExerciseMuscleTargetEntity;
import com.ironcore.infrastructure.persistence.exercisemuscletarget.mapper.ExerciseMuscleTargetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExerciseMuscleTargetAdapter implements ExerciseMuscleTargetRepository {

    private final ExerciseMuscleTargetJpaRepository exerciseMuscleTargetJpaRepository;

    @Override
    public Optional<ExerciseMuscleTarget> findById(ExerciseMuscleTargetId id) {
        Optional<ExerciseMuscleTargetEntity> entity;
        try {
            Long exerciseMuscleTargetId = Objects.requireNonNull(
                    id.value(),
                    "Id de músculo alvo do exercício ativo não pode ser nulo."
            );
            entity = exerciseMuscleTargetJpaRepository.findById(exerciseMuscleTargetId);
        } catch (RuntimeException exception) {
            throw new PersistenceException(
                    "Falha ao buscar músculo alvo do exercício por id",
                    exception
            );
        }

        try {
            return entity.map(ExerciseMuscleTargetMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException(
                    "Falha ao converter músculo alvo do exercício de entidade para domínio.",
                    exception
            );
        }
    }
}
