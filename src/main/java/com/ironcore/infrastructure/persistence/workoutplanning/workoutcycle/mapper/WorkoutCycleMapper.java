package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.mapper;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.entity.TrainingGoalEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;

public class WorkoutCycleMapper {

    public static WorkoutCycleEntity toEntity(
            WorkoutCycle workoutCycle,
            PersonEntity person,
            TrainingGoalEntity trainingGoal
    ) {
        try {
            return new WorkoutCycleEntity(
                    workoutCycle.getId() == null ? null : workoutCycle.getId().value(),
                    person,
                    workoutCycle.getName(),
                    trainingGoal,
                    workoutCycle.getStartDate(),
                    workoutCycle.getEndDate(),
                    workoutCycle.getDesiredDurationMonths(),
                    workoutCycle.getWorkoutStatus(),
                    workoutCycle.getWorkoutOrigin(),
                    workoutCycle.getNotes(),
                    workoutCycle.getCreatedAt(),
                    workoutCycle.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter ciclo de treino de domínio para entidade.", exception);
        }
    }

    public static WorkoutCycle toDomain(WorkoutCycleEntity entity) {
        try {
            return WorkoutCycle.restore(
                    new WorkoutCycleId(entity.getId()),
                    new PersonId(entity.getPerson().getId()),
                    entity.getName(),
                    new TrainingGoalId(entity.getTrainingGoal().getId()),
                    entity.getStartDate(),
                    entity.getEndDate(),
                    entity.getDesiredDurationMonths(),
                    entity.getWorkoutStatus(),
                    entity.getWorkoutOrigin(),
                    entity.getNotes(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter ciclo de treino de entidade para domínio.", exception);
        }
    }
}
