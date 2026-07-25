package com.ironcore.application.exercise.catalog.result;

import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;

public record MuscleSubgroupItemResult(
        MuscleSubgroupId id,
        MuscleSubgroupCode code,
        MuscleGroupId muscleGroupId,
        String name
) {
}
