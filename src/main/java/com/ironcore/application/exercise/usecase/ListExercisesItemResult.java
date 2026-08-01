package com.ironcore.application.exercise.usecase;

import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.EquipmentTypeItemResult;
import com.ironcore.domain.exercise.valueobject.ExerciseId;

public record ListExercisesItemResult(
        ExerciseId id,
        String name,
        EquipmentTypeItemResult equipmentType,
        ActivityTypeItemResult activityType,
        Boolean unilateral,
        Boolean compound,
        Integer suggestedRestSeconds
) {
}
