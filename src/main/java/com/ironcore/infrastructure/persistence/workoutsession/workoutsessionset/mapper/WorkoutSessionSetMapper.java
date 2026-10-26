package com.ironcore.infrastructure.persistence.workoutsession.workoutsessionset.mapper;

import com.ironcore.domain.workoutsession.workoutsessionset.model.WorkoutSessionSet;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.entity.WorkoutSessionActivityEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionset.entity.WorkoutSessionSetEntity;

public final class WorkoutSessionSetMapper {
    private WorkoutSessionSetMapper() {}

    public static WorkoutSessionSetEntity toEntity(WorkoutSessionSet source, WorkoutSessionActivityEntity parent) {
        try {
            WorkoutSessionSetEntity entity = new WorkoutSessionSetEntity();
            entity.setId(source.getId() == null ? null : source.getId().value());
            entity.setActivity(parent);
            entity.setSetNumber(source.getSetNumber());
            entity.setCreatedAt(source.getCreatedAt());
            applyExecution(source, entity);
            return entity;
        } catch (RuntimeException ex) {
            throw new DataMappingException("Falha ao mapear série para persistência.", ex);
        }
    }

    public static void applyExecution(WorkoutSessionSet source, WorkoutSessionSetEntity entity) {
        entity.setActualLoadKg(source.getActualLoadKg());
        entity.setActualLoadMode(source.getActualLoadMode());
        entity.setActualReps(source.getActualReps());
        entity.setNotes(source.getNotes());
        entity.setUpdatedAt(source.getUpdatedAt());
    }

    public static WorkoutSessionSet toDomain(WorkoutSessionSetEntity entity) {
        try {
            return WorkoutSessionSet.restore(new WorkoutSessionSetId(entity.getId()), entity.getSetNumber(),
                    entity.getActualLoadKg(), entity.getActualLoadMode(), entity.getActualReps(), entity.getNotes(),
                    entity.getCreatedAt(), entity.getUpdatedAt());
        } catch (RuntimeException ex) {
            throw new DataMappingException("Falha ao restaurar série persistida.", ex);
        }
    }
}
