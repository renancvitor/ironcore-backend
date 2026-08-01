package com.ironcore.application.exercise.usecase;

import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;

public record ListExercisesCommand(
        String name,
        ActivityTypeId activityTypeId,
        EquipmentTypeId equipmentTypeId,
        MuscleGroupId muscleGroupId,
        MuscleSubgroupId muscleSubgroupId,
        TargetRoleType targetRole,
        int page,
        int size
) {
}
