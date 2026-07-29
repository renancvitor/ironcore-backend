package com.ironcore.interfaces.rest.exercise.dto;

import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.interfaces.rest.exercise.catalog.dto.MuscleSubgroupItemResponse;

public record ExerciseMuscleTargetItemResponse(
        MuscleSubgroupItemResponse muscleSubgroup,
        TargetRoleType targetRole
) {
}
