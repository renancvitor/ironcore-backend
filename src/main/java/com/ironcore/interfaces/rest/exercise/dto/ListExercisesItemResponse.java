package com.ironcore.interfaces.rest.exercise.dto;

import com.ironcore.interfaces.rest.exercise.catalog.dto.ActivityTypeItemResponse;
import com.ironcore.interfaces.rest.exercise.catalog.dto.EquipmentTypeItemResponse;

public record ListExercisesItemResponse(
        Long id,
        String name,
        EquipmentTypeItemResponse equipmentType,
        ActivityTypeItemResponse activityType,
        Boolean unilateral,
        Boolean compound,
        Integer suggestedRestSeconds
) {
}
