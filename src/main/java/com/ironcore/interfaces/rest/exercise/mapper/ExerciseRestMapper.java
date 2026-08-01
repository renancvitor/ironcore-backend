package com.ironcore.interfaces.rest.exercise.mapper;

import com.ironcore.application.exercise.usecase.*;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.interfaces.rest.exercise.catalog.mapper.ExerciseCatalogRestMapper;
import com.ironcore.interfaces.rest.exercise.dto.*;

public final class ExerciseRestMapper {

    private ExerciseRestMapper() {
    }

    public static GetExerciseByIdResponse toGetByIdResponse(
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
                        .map(ExerciseRestMapper::toMuscleTargetResponse)
                        .toList()
        );
    }

    public static ExerciseMuscleTargetItemResponse toMuscleTargetResponse(
            ExerciseMuscleTargetItemResult result
    ) {
        return new ExerciseMuscleTargetItemResponse(
                ExerciseCatalogRestMapper.toResponse(result.muscleSubgroup()),
                result.targetRole()
        );
    }

    public static ListExercisesCommand toCommand(
            ListExercisesRequest request,
            int page,
            int size
    ) {
        return new ListExercisesCommand(
                request.name(),
                request.activityTypeId() == null ? null : new ActivityTypeId(request.activityTypeId()),
                request.equipmentTypeId() == null ? null : new EquipmentTypeId(request.equipmentTypeId()),
                request.muscleGroupId() == null ? null : new MuscleGroupId(request.muscleGroupId()),
                request.muscleSubgroupId() == null ? null : new MuscleSubgroupId(request.muscleSubgroupId()),
                request.targetRole(),
                page,
                size
        );
    }

    public static ListExercisesResponse toListResponse(
            ListExercisesResult result
    ) {
        PageResult<ListExercisesItemResult> page = result.exercises();

        PageResult<ListExercisesItemResponse> responsePage = new PageResult<>(
                page.content()
                        .stream()
                        .map(ExerciseRestMapper::toItemListResponse)
                        .toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.last()
        );

        return  new ListExercisesResponse(responsePage);
    }

    public static ListExercisesItemResponse toItemListResponse(
            ListExercisesItemResult result
    ) {
        return new ListExercisesItemResponse(
                result.id() == null ? null : result.id().value(),
                result.name(),
                ExerciseCatalogRestMapper.toResponse(result.equipmentType()),
                ExerciseCatalogRestMapper.toResponse(result.activityType()),
                result.unilateral(),
                result.compound(),
                result.suggestedRestSeconds()
        );
    }
}
