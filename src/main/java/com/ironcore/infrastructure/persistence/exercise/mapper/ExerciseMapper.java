package com.ironcore.infrastructure.persistence.exercise.mapper;

import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;
import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;

public class ExerciseMapper {

    public static ExerciseEntity toEntity(
            Exercise exercise,
            EquipmentTypeEntity equipmentType,
            ActivityTypeEntity activityType
    ) {
        try {
            return new ExerciseEntity(
                    exercise.getId() == null ? null : exercise.getId().value(),
                    exercise.getName(),
                    equipmentType,
                    activityType,
                    exercise.getUnilateral(),
                    exercise.getCompound(),
                    exercise.getSuggestedRestSeconds(),
                    exercise.getActive(),
                    exercise.getCreatedAt(),
                    exercise.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter exercício de domínio para entidade.", exception);
        }
    }

    public static Exercise toDomain(ExerciseEntity exerciseEntity) {
        try {
            return new Exercise(
                new ExerciseId(exerciseEntity.getId()),
                exerciseEntity.getName(),
                new EquipmentTypeId(exerciseEntity.getEquipmentType().getId()),
                new ActivityTypeId(exerciseEntity.getActivityType().getId()),
                exerciseEntity.getUnilateral(),
                exerciseEntity.getCompound(),
                exerciseEntity.getSuggestedRestSeconds(),
                exerciseEntity.getActive(),
                exerciseEntity.getCreatedAt(),
                exerciseEntity.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter exercício de entidade para domínio.", exception);
        }
    }
}
