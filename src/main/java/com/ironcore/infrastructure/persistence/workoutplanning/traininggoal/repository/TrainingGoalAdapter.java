package com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.repository;

import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.repository.TrainingGoalRepository;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.entity.TrainingGoalEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.mapper.TrainingGoalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainingGoalAdapter implements TrainingGoalRepository {

    private final TrainingGoalJpaRepository trainingGoalJpaRepository;

    @Override
    public Optional<TrainingGoal> findById(TrainingGoalId id) {
        Optional<TrainingGoalEntity> entity;

        try {
            Long trainingGoalId = Objects.requireNonNull(
                    id.value(),
                    "Id do objetivo de treino não pode ser nulo."
            );
            entity = trainingGoalJpaRepository.findById(trainingGoalId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar training goal por id.", exception);
        }

        try {
            return entity.map(TrainingGoalMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter training goal por id para domínio.", exception);
        }
    }

    @Override
    public List<TrainingGoal> findAllActive() {
        List<TrainingGoalEntity> entities;

        try {
            entities = trainingGoalJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc();
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao listar training goals ativos.", exception);
        }

        try {
            return entities.stream().map(TrainingGoalMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter lista de training goals ativos para domínio.", exception);
        }
    }
}
