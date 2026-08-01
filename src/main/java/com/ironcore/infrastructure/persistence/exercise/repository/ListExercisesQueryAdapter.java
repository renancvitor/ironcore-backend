package com.ironcore.infrastructure.persistence.exercise.repository;

import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.EquipmentTypeItemResult;
import com.ironcore.application.exercise.port.ListExercisesQueryPort;
import com.ironcore.application.exercise.usecase.ListExercisesItemResult;
import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;
import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.exercise.specification.ExerciseSpecifications;
import com.ironcore.infrastructure.persistence.shared.pagination.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ListExercisesQueryAdapter implements ListExercisesQueryPort {

    private final ExerciseJpaRepository exerciseJpaRepository;

    @Override
    public PageResult<ListExercisesItemResult> findActiveExercises(
            String name,
            ActivityTypeId activityTypeId,
            EquipmentTypeId equipmentTypeId,
            MuscleGroupId muscleGroupId,
            MuscleSubgroupId muscleSubgroupId,
            TargetRoleType targetRole,
            PageQuery pageQuery
    ) {
        Page<ExerciseEntity> entities;

        try {
            Specification<ExerciseEntity> specification =
                    ExerciseSpecifications.filter(
                            name,
                            valueOf(activityTypeId),
                            valueOf(equipmentTypeId),
                            valueOf(muscleGroupId),
                            valueOf(muscleSubgroupId),
                            targetRole
                    );

            Pageable pageable = PageRequest.of(
                    pageQuery.page(),
                    pageQuery.size(),
                    Sort.by("name").ascending()
                            .and(Sort.by("id").ascending())
            );

            entities = exerciseJpaRepository.findAll(specification, pageable);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar exercícios.", exception);
        }

        try {
            Page<ListExercisesItemResult> result = entities.map(ListExercisesQueryAdapter::toResult);

            return PageMapper.toPageResult(result);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao projetar exercícios.", exception);
        }
    }

    private static Long valueOf(ActivityTypeId id) {
        return id == null ? null : id.value();
    }

    private static Long valueOf(EquipmentTypeId id) {
        return id == null ? null : id.value();
    }

    private static Long valueOf(MuscleGroupId id) {
        return id == null ? null : id.value();
    }

    private static Long valueOf(MuscleSubgroupId id) {
        return id == null ? null : id.value();
    }

    private static ListExercisesItemResult toResult(ExerciseEntity entity) {
        return new ListExercisesItemResult(
                new ExerciseId(entity.getId()),
                entity.getName(),
                toResult(entity.getEquipmentType()),
                toResult(entity.getActivityType()),
                entity.getUnilateral(),
                entity.getCompound(),
                entity.getSuggestedRestSeconds()
        );
    }

    private static EquipmentTypeItemResult toResult(EquipmentTypeEntity entity) {
        return new EquipmentTypeItemResult(
                new EquipmentTypeId(entity.getId()),
                new EquipmentTypeCode(entity.getCode()),
                entity.getDisplayName()
        );
    }

    private static ActivityTypeItemResult toResult(ActivityTypeEntity entity) {
        return new ActivityTypeItemResult(
                new ActivityTypeId(entity.getId()),
                new ActivityTypeCode(entity.getCode()),
                entity.getDisplayName()
        );
    }
}
