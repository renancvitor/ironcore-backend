package com.ironcore.infrastructure.persistence.workoutplanning.workoutday.mapper;

import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity.WorkoutDayEntity;

public class WorkoutDayMapper {

    public static WorkoutDayEntity toEntity(WorkoutDay workoutDay, WorkoutCycleEntity workoutCycle) {
        try {
            return new WorkoutDayEntity(
                    workoutDay.getId() == null ? null : workoutDay.getId().value(),
                    workoutCycle,
                    workoutDay.getWeekDay(),
                    workoutDay.getTitle(),
                    workoutDay.getOrderIndex(),
                    workoutDay.getCreatedAt(),
                    workoutDay.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter dia de treino de domínio para entidade.", exception);
        }
    }

    public static WorkoutDay toDomain(WorkoutDayEntity entity) {
        try {
            return WorkoutDay.restore(
                    new WorkoutDayId(entity.getId()),
                    new WorkoutCycleId(entity.getWorkoutCycle().getId()),
                    entity.getWeekDay(),
                    entity.getTitle(),
                    entity.getOrderIndex(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter dia de treino de entidade para domínio.", exception);
        }
    }
}
