package com.ironcore.application.exercise.port;

import com.ironcore.application.exercise.usecase.ListExercisesItemResult;
import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;

public interface ListExercisesQueryPort {

    PageResult<ListExercisesItemResult> findActiveExercises(
            String name,
            ActivityTypeId activityTypeId,
            EquipmentTypeId equipmentTypeId,
            MuscleGroupId muscleGroupId,
            MuscleSubgroupId muscleSubgroupId,
            TargetRoleType targetRole,
            PageQuery pageQuery
    );
}
