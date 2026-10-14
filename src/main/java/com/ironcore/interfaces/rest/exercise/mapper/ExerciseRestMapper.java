package com.ironcore.interfaces.rest.exercise.mapper;

import com.ironcore.application.exercise.usecase.*;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.interfaces.rest.exercise.catalog.mapper.ExerciseCatalogRestMapper;
import com.ironcore.interfaces.rest.exercise.dto.*;

import java.util.List;

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
                resolve(request.activityTypeId(), request.activityTypeIds(), "activityTypeId").stream().map(ActivityTypeId::new).toList(),
                resolve(request.equipmentTypeId(), request.equipmentTypeIds(), "equipmentTypeId").stream().map(EquipmentTypeId::new).toList(),
                resolve(request.muscleGroupId(), request.muscleGroupIds(), "muscleGroupId").stream().map(MuscleGroupId::new).toList(),
                resolve(request.muscleSubgroupId(), request.muscleSubgroupIds(), "muscleSubgroupId").stream().map(MuscleSubgroupId::new).toList(),
                resolve(request.targetRole(), request.targetRoles(), "targetRole"),
                page,
                size
        );
    }

    private static <T> List<T> resolve(T single, List<T> multiple, String parameter) {
        if (single != null && multiple != null) {
            throw new IllegalArgumentException("Use apenas " + parameter + " ou " + parameter + "s, nunca ambos.");
        }
        List<T> values = multiple != null ? multiple : single == null ? List.of() : List.of(single);
        if (values.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Valores vazios em " + parameter + "s nao sao permitidos.");
        }
        return values.stream().distinct().toList();
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
