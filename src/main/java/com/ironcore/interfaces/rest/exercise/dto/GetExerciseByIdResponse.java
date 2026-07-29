package com.ironcore.interfaces.rest.exercise.dto;

import com.ironcore.interfaces.rest.exercise.catalog.dto.ActivityTypeItemResponse;
import com.ironcore.interfaces.rest.exercise.catalog.dto.EquipmentTypeItemResponse;

import java.util.List;

public record GetExerciseByIdResponse(
        Long id,
        String name,
        EquipmentTypeItemResponse equipmentType,
        ActivityTypeItemResponse activityType,
        Boolean unilateral,
        Boolean compound,
        Integer suggestedRestSeconds,
        Boolean active,
        List<ExerciseMuscleTargetItemResponse> muscleTargets
) {
}
