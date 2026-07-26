package com.ironcore.application.exercise.catalog.usecase;

import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;

public record MuscleGroupItemResult(
        MuscleGroupId id,
        MuscleGroupCode code,
        String name
) {
}
