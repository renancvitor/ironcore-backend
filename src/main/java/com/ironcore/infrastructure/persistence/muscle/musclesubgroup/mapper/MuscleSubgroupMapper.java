package com.ironcore.infrastructure.persistence.muscle.musclesubgroup.mapper;

import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.model.MuscleSubgroup;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.muscle.musclegroup.entity.MuscleGroupEntity;
import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.entity.MuscleSubgroupEntity;

public class MuscleSubgroupMapper {

    public static MuscleSubgroupEntity toEntity(MuscleSubgroup muscleSubgroup, MuscleGroupEntity muscleGroup) {
        try {
            return new MuscleSubgroupEntity(
                    muscleSubgroup.getId() == null ? null : muscleSubgroup.getId().value(),
                    muscleGroup,
                    muscleSubgroup.getCode() == null ? null : muscleSubgroup.getCode().value(),
                    muscleSubgroup.getDisplayName(),
                    muscleSubgroup.getActive(),
                    muscleSubgroup.getSortOrder()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter subgrupo muscular de domínio para entidade.", exception);
        }
    }

    public static MuscleSubgroup toDomain(MuscleSubgroupEntity muscleSubgroupEntity) {
        try {
            return MuscleSubgroup.restore(
                    new MuscleSubgroupId(muscleSubgroupEntity.getId()),
                    new MuscleGroupId(muscleSubgroupEntity.getMuscleGroup().getId()),
                    new MuscleSubgroupCode(muscleSubgroupEntity.getCode()),
                    muscleSubgroupEntity.getDisplayName(),
                    muscleSubgroupEntity.getActive(),
                    muscleSubgroupEntity.getSortOrder()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter subgrupo muscular de entidade para domínio.", exception);
        }
    }
}
