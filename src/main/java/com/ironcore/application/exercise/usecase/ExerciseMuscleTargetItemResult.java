package com.ironcore.application.exercise.usecase;

import com.ironcore.application.exercise.catalog.usecase.MuscleSubgroupItemResult;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;

public record ExerciseMuscleTargetItemResult(
        MuscleSubgroupItemResult muscleSubgroup,
        TargetRoleType targetRole
) {
}
