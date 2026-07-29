package com.ironcore.application.exercise.usecase;

import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.EquipmentTypeItemResult;
import com.ironcore.domain.exercise.valueobject.ExerciseId;

import java.util.List;

public record GetExerciseByIdResult(
        ExerciseId id,
        String name,
        EquipmentTypeItemResult equipmentType,
        ActivityTypeItemResult activityType,
        Boolean unilateral,
        Boolean compound,
        Integer suggestedRestSeconds,
        Boolean active,
        List<ExerciseMuscleTargetItemResult> muscleTargets
) {
}
