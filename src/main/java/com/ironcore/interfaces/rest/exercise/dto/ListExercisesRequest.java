package com.ironcore.interfaces.rest.exercise.dto;

import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

public record ListExercisesRequest(
        String name,
        Long activityTypeId,
        Long equipmentTypeId,
        Long muscleGroupId,
        Long muscleSubgroupId,
        TargetRoleType targetRole,
        @Schema(description = "Multiple values (OR). Repeat the query parameter. Cannot combine with activityTypeId.")
        List<Long> activityTypeIds,
        @Schema(description = "Multiple values (OR). Repeat the query parameter. Cannot combine with equipmentTypeId.")
        List<Long> equipmentTypeIds,
        @Schema(description = "Multiple values (OR). Repeat the query parameter. Cannot combine with muscleGroupId.")
        List<Long> muscleGroupIds,
        @Schema(description = "Multiple values (OR). Repeat the query parameter. Cannot combine with muscleSubgroupId.")
        List<Long> muscleSubgroupIds,
        @Schema(description = "Multiple values (OR). Repeat the query parameter. Cannot combine with targetRole.")
        List<TargetRoleType> targetRoles
) {
}
