package com.ironcore.infrastructure.persistence.muscle.musclegroup.mapper;

import com.ironcore.domain.muscle.musclegroup.model.MuscleGroup;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.muscle.musclegroup.entity.MuscleGroupEntity;

public class MuscleGroupMapper {

    public static MuscleGroupEntity toEntity(MuscleGroup muscleGroup) {
        try {
            return new MuscleGroupEntity(
                    muscleGroup.getId() == null ? null : muscleGroup.getId().value(),
                    muscleGroup.getCode() == null ? null : muscleGroup.getCode().value(),
                    muscleGroup.getDisplayName(),
                    muscleGroup.getActive(),
                    muscleGroup.getSortOrder()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter grupo muscular de domínio para entidade.", exception);
        }
    }

    public static MuscleGroup toDomain(MuscleGroupEntity muscleGroupEntity) {
        try {
            return MuscleGroup.restore(
                    new MuscleGroupId(muscleGroupEntity.getId()),
                    new MuscleGroupCode(muscleGroupEntity.getCode()),
                    muscleGroupEntity.getDisplayName(),
                    muscleGroupEntity.getActive(),
                    muscleGroupEntity.getSortOrder()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter grupo muscular de entidade para domínio.", exception);
        }
    }
}
