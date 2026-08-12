package com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.mapper;

import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalCode;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.entity.TrainingGoalEntity;

public class TrainingGoalMapper {

    public static TrainingGoalEntity toEntity(TrainingGoal trainingGoal) {
        try {
            return new TrainingGoalEntity(
                    trainingGoal.getId() == null ? null : trainingGoal.getId().value(),
                    trainingGoal.getCode() == null ? null : trainingGoal.getCode().value(),
                    trainingGoal.getDisplayName(),
                    trainingGoal.getActive(),
                    trainingGoal.getSortOrder()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter objetivo de treino de domínio para entidade.", exception);
        }
    }

    public static TrainingGoal toDomain(TrainingGoalEntity entity) {
        try {
            return TrainingGoal.restore(
                    new TrainingGoalId(entity.getId()),
                    new TrainingGoalCode(entity.getCode()),
                    entity.getDisplayName(),
                    entity.getActive(),
                    entity.getSortOrder()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter objetivo de treino de entidade para domínio.", exception);
        }
    }
}
