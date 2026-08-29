package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail;

import java.util.List;

public record ExerciseDetailResponse(
        Long id,
        String name,
        List<MuscleGroupDetailResponse> muscleGroups
) {
}
