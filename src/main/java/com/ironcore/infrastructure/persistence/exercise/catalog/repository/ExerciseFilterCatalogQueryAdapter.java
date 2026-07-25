package com.ironcore.infrastructure.persistence.exercise.catalog.repository;

import com.ironcore.application.exercise.catalog.port.ExerciseFilterCatalogQueryPort;
import com.ironcore.application.exercise.catalog.result.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.result.EquipmentTypeItemResult;
import com.ironcore.application.exercise.catalog.result.MuscleGroupItemResult;
import com.ironcore.application.exercise.catalog.result.MuscleSubgroupItemResult;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;
import com.ironcore.infrastructure.persistence.activitytype.repository.ActivityTypeJpaRepository;
import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;
import com.ironcore.infrastructure.persistence.equipmenttype.repository.EquipmentTypeJpaRepository;
import com.ironcore.infrastructure.persistence.muscle.musclegroup.entity.MuscleGroupEntity;
import com.ironcore.infrastructure.persistence.muscle.musclegroup.repository.MuscleGroupJpaRepository;
import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.entity.MuscleSubgroupEntity;
import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.repository.MuscleSubgroupJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ExerciseFilterCatalogQueryAdapter implements ExerciseFilterCatalogQueryPort {

    private final ActivityTypeJpaRepository activityTypeJpaRepository;
    private final EquipmentTypeJpaRepository equipmentTypeJpaRepository;
    private final MuscleGroupJpaRepository muscleGroupJpaRepository;
    private final MuscleSubgroupJpaRepository muscleSubgroupJpaRepository;

    private static ActivityTypeItemResult toResult(ActivityTypeEntity entity) {
        return new ActivityTypeItemResult(
                new ActivityTypeId(entity.getId()),
                new ActivityTypeCode(entity.getCode()),
                entity.getDisplayName()
        );
    }

    private static EquipmentTypeItemResult toResult(EquipmentTypeEntity entity) {
        return new EquipmentTypeItemResult(
                new EquipmentTypeId(entity.getId()),
                new EquipmentTypeCode(entity.getCode()),
                entity.getDisplayName()
        );
    }

    private static MuscleGroupItemResult toResult(MuscleGroupEntity entity) {
        return new MuscleGroupItemResult(
                new MuscleGroupId(entity.getId()),
                new MuscleGroupCode(entity.getCode()),
                entity.getDisplayName()
        );
    }

    private static MuscleSubgroupItemResult toResult(MuscleSubgroupEntity entity) {
        return new MuscleSubgroupItemResult(
                new MuscleSubgroupId(entity.getId()),
                new MuscleSubgroupCode(entity.getCode()),
                new MuscleGroupId(entity.getMuscleGroup().getId()),
                entity.getDisplayName()
        );
    }

    @Override
    public List<ActivityTypeItemResult> findActiveActivityTypes() {
        List<ActivityTypeEntity> entities;

        try {
            entities = activityTypeJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc();
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar tipos de atividade ativos.", exception);
        }

        try {
            return entities.stream().map(ExerciseFilterCatalogQueryAdapter::toResult).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao projetar tipos de atividade ativos.", exception);
        }
    }

    @Override
    public List<EquipmentTypeItemResult> findActiveEquipmentTypes() {
        List<EquipmentTypeEntity> entities;

        try {
            entities = equipmentTypeJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc();
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar tipos de equipamento ativos.", exception);
        }

        try {
            return entities.stream().map(ExerciseFilterCatalogQueryAdapter::toResult).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao projetar tipos de equipamento ativos.", exception);
        }
    }

    @Override
    public List<MuscleGroupItemResult> findActiveMuscleGroups() {
        List<MuscleGroupEntity> entities;

        try {
            entities = muscleGroupJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc();
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar grupos musculares ativos.", exception);
        }

        try {
            return entities.stream().map(ExerciseFilterCatalogQueryAdapter::toResult).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao projetar grupos musculares ativos.", exception);
        }
    }

    @Override
    public List<MuscleSubgroupItemResult> findActiveMuscleSubgroups() {
        List<MuscleSubgroupEntity> entities;

        try {
            entities = muscleSubgroupJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc();
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar subgrupos musculares ativos.", exception);
        }

        try {
            return entities.stream().map(ExerciseFilterCatalogQueryAdapter::toResult).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao projetar subgrupos musculares ativos.", exception);
        }
    }

    @Override
    public List<MuscleSubgroupItemResult> findActiveMuscleSubgroupsByMuscleGroupId(MuscleGroupId id) {
        List<MuscleSubgroupEntity> entities;

        try {
            Long muscleGroupId = Objects.requireNonNull(id.value());
            entities = muscleSubgroupJpaRepository
                    .findAllByMuscleGroup_IdAndActiveTrueOrderBySortOrderAscDisplayNameAsc(muscleGroupId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar subgrupos musculares ativos por grupo muscular.", exception);
        }

        try {
            return entities.stream().map(ExerciseFilterCatalogQueryAdapter::toResult).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao projetar subgrupos musculares ativos por grupo muscular.", exception);
        }
    }
}
