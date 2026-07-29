package com.ironcore.interfaces.rest.exercise.mapper;

import com.ironcore.application.exercise.usecase.ExerciseMuscleTargetItemResult;
import com.ironcore.application.exercise.usecase.GetExerciseByIdResult;
import com.ironcore.interfaces.rest.exercise.catalog.mapper.ExerciseCatalogRestMapper;
import com.ironcore.interfaces.rest.exercise.dto.ExerciseMuscleTargetItemResponse;
import com.ironcore.interfaces.rest.exercise.dto.GetExerciseByIdResponse;

public final class ExerciseRestMapper {

    private ExerciseRestMapper() {
    }

    public static GetExerciseByIdResponse toResponse(
            GetExerciseByIdResult result
    ) {
        return new GetExerciseByIdResponse(
                result.id().value(),
                result.name(),
                ExerciseCatalogRestMapper.toResponse(result.equipmentType()),
                ExerciseCatalogRestMapper.toResponse(result.activityType()),
                result.unilateral(),
                result.compound(),
                result.suggestedRestSeconds(),
                result.active(),
                result.muscleTargets().stream()
                        .map(ExerciseRestMapper::toResponse)
                        .toList()
        );
    }

    public static ExerciseMuscleTargetItemResponse toResponse(
            ExerciseMuscleTargetItemResult result
    ) {
        return new ExerciseMuscleTargetItemResponse(
                ExerciseCatalogRestMapper.toResponse(result.muscleSubgroup()),
                result.targetRole()
        );
    }
}
