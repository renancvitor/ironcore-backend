package com.ironcore.application.exercise.usecase;

import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;

import java.util.List;

public record ListExercisesCommand(
        String name,
        List<ActivityTypeId> activityTypeIds,
        List<EquipmentTypeId> equipmentTypeIds,
        List<MuscleGroupId> muscleGroupIds,
        List<MuscleSubgroupId> muscleSubgroupIds,
        List<TargetRoleType> targetRoles,
        int page,
        int size
) {
    public ListExercisesCommand {
        activityTypeIds = activityTypeIds == null ? List.of() : List.copyOf(activityTypeIds);
        equipmentTypeIds = equipmentTypeIds == null ? List.of() : List.copyOf(equipmentTypeIds);
        muscleGroupIds = muscleGroupIds == null ? List.of() : List.copyOf(muscleGroupIds);
        muscleSubgroupIds = muscleSubgroupIds == null ? List.of() : List.copyOf(muscleSubgroupIds);
        targetRoles = targetRoles == null ? List.of() : List.copyOf(targetRoles);
    }
}
