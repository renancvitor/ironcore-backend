package com.ironcore.infrastructure.persistence.activitytype.mapper;

import com.ironcore.domain.activitytype.model.ActivityType;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;

public class ActivityTypeMapper {

    public static ActivityTypeEntity toEntity(ActivityType activityType) {
        try {
            return new ActivityTypeEntity(
                    activityType.getId() == null ? null : activityType.getId().value(),
                    activityType.getCode() == null ? null : activityType.getCode().value(),
                    activityType.getDisplayName(),
                    activityType.getActive(),
                    activityType.getSortOrder()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter tipo de atividade de domínio para entidade.", exception);
        }
    }

    public static ActivityType toDomain(ActivityTypeEntity entity) {
        try {
            return new ActivityType(
                    new ActivityTypeId(entity.getId()),
                    new ActivityTypeCode(entity.getCode()),
                    entity.getDisplayName(),
                    entity.getActive(),
                    entity.getSortOrder()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter tipo de atividade de entidade para domínio.", exception);
        }
    }
}
