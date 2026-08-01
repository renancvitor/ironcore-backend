package com.ironcore.interfaces.rest.exercise.dto;

import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;

public record ListExercisesRequest(
        String name,
        Long activityTypeId,
        Long equipmentTypeId,
        Long muscleGroupId,
        Long muscleSubgroupId,
        TargetRoleType targetRole
) {
}
