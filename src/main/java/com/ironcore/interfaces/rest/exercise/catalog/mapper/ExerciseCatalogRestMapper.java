package com.ironcore.interfaces.rest.exercise.catalog.mapper;

import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.EquipmentTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.MuscleGroupItemResult;
import com.ironcore.application.exercise.catalog.usecase.MuscleSubgroupItemResult;
import com.ironcore.interfaces.rest.exercise.catalog.dto.ActivityTypeItemResponse;
import com.ironcore.interfaces.rest.exercise.catalog.dto.EquipmentTypeItemResponse;
import com.ironcore.interfaces.rest.exercise.catalog.dto.MuscleGroupItemResponse;
import com.ironcore.interfaces.rest.exercise.catalog.dto.MuscleSubgroupItemResponse;

public final class ExerciseCatalogRestMapper {

    private ExerciseCatalogRestMapper() {
    }

    public static ActivityTypeItemResponse toResponse(
            ActivityTypeItemResult result
    ) {
        return new ActivityTypeItemResponse(
                result.id().value(),
                result.code().value(),
                result.name()
        );
    }

    public static EquipmentTypeItemResponse toResponse(
            EquipmentTypeItemResult result
    ) {
        return new EquipmentTypeItemResponse(
                result.id().value(),
                result.code().value(),
                result.name()
        );
    }

    public static MuscleGroupItemResponse toResponse(
            MuscleGroupItemResult result
    ) {
        return new MuscleGroupItemResponse(
                result.id().value(),
                result.code().value(),
                result.name()
        );
    }

    public static MuscleSubgroupItemResponse toResponse(
            MuscleSubgroupItemResult result
    ) {
        return new MuscleSubgroupItemResponse(
                result.id().value(),
                result.code().value(),
                result.muscleGroupId().value(),
                result.name()
        );
    }
}
