package com.ironcore.infrastructure.persistence.exercise.repository;

import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.EquipmentTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.MuscleSubgroupItemResult;
import com.ironcore.application.exercise.port.GetExerciseByIdQueryPort;
import com.ironcore.application.exercise.usecase.ExerciseMuscleTargetItemResult;
import com.ironcore.application.exercise.usecase.GetExerciseByIdResult;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;
import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.exercisemuscletarget.entity.ExerciseMuscleTargetEntity;
import com.ironcore.infrastructure.persistence.exercisemuscletarget.repository.ExerciseMuscleTargetJpaRepository;
import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.entity.MuscleSubgroupEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GetExerciseByIdAdapter implements GetExerciseByIdQueryPort {

    private final ExerciseJpaRepository exerciseJpaRepository;
    private final ExerciseMuscleTargetJpaRepository exerciseMuscleTargetJpaRepository;

    private static GetExerciseByIdResult toResult(
            ExerciseEntity exercise,
            List<ExerciseMuscleTargetEntity> muscleTargets
    ) {
        return new GetExerciseByIdResult(
                new ExerciseId(exercise.getId()),
                exercise.getName(),
                toResult(exercise.getEquipmentType()),
                toResult(exercise.getActivityType()),
                exercise.getUnilateral(),
                exercise.getCompound(),
                exercise.getSuggestedRestSeconds(),
                exercise.getActive(),
                muscleTargets.stream()
                        .map(GetExerciseByIdAdapter::toResult)
                        .toList()
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

    private static ExerciseMuscleTargetItemResult toResult(ExerciseMuscleTargetEntity entity) {
        return new ExerciseMuscleTargetItemResult(
                toResult(entity.getMuscleSubgroup()),
                entity.getTargetRole()
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
    public Optional<GetExerciseByIdResult> findActiveDetailById(ExerciseId id) {
        ExerciseEntity exercise;
        List<ExerciseMuscleTargetEntity> muscleTargets;

        try {
            Long exerciseId = Objects.requireNonNull(id.value(), "Id do exercício não pode ser nulo.");
            Optional<ExerciseEntity> exerciseOptional = exerciseJpaRepository.findByIdAndActiveTrue(exerciseId);

            if (exerciseOptional.isEmpty()) {
                return Optional.empty();
            }

            exercise = exerciseOptional.get();
            muscleTargets = exerciseMuscleTargetJpaRepository
                    .findAllByExercise_IdAndActiveTrueOrderByTargetRoleAscMuscleSubgroup_DisplayNameAsc(exerciseId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar detalhe do exercício por id.", exception);
        }

        try {
            return Optional.of(toResult(exercise, muscleTargets));
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao projetar detalhe do exercício por id.", exception);
        }
    }
}
