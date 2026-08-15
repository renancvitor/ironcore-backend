package com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.mapper;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.entity.WorkoutActivityEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity.WorkoutDayEntity;

public class WorkoutActivityMapper {

    public static WorkoutActivityEntity toEntity(
            WorkoutActivity workoutActivity,
            WorkoutDayEntity workoutDay,
            ExerciseEntity exercise
    ) {
        try {
            return new WorkoutActivityEntity(
                    workoutActivity.getId() == null ? null : workoutActivity.getId().value(),
                    workoutDay,
                    exercise,
                    workoutActivity.getOrderIndex(),
                    workoutActivity.getSets(),
                    workoutActivity.getRepRangeMin(),
                    workoutActivity.getRepRangeMax(),
                    workoutActivity.getTargetLoadKg(),
                    workoutActivity.getTargetLoadText(),
                    workoutActivity.getDurationMinutes(),
                    workoutActivity.getDistanceKm(),
                    workoutActivity.getIntensityText(),
                    workoutActivity.getRestSeconds(),
                    workoutActivity.getNotes(),
                    workoutActivity.getCreatedAt(),
                    workoutActivity.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter atividade de treino de domínio para entidade.", exception);
        }
    }

    public static WorkoutActivity toDomain(WorkoutActivityEntity entity) {
        try {
            return WorkoutActivity.restore(
                    new WorkoutActivityId(entity.getId()),
                    new WorkoutDayId(entity.getWorkoutDay().getId()),
                    new ExerciseId(entity.getExercise().getId()),
                    entity.getOrderIndex(),
                    entity.getSets(),
                    entity.getRepRangeMin(),
                    entity.getRepRangeMax(),
                    entity.getTargetLoadKg(),
                    entity.getTargetLoadText(),
                    entity.getDurationMinutes(),
                    entity.getDistanceKm(),
                    entity.getIntensityText(),
                    entity.getRestSeconds(),
                    entity.getNotes(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter atividade de treino de entidade para domínio.", exception);
        }
    }
}
