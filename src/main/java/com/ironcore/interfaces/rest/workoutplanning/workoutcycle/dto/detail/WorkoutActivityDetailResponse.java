package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail;

import java.math.BigDecimal;

public record WorkoutActivityDetailResponse(
        Long id,
        Integer orderIndex,
        Integer sets,
        Integer repRangeMin,
        Integer repRangeMax,
        BigDecimal targetLoadKg,
        String targetLoadText,
        Integer durationMinutes,
        BigDecimal distanceKm,
        String intensityText,
        Integer restSeconds,
        String notes,
        ExerciseDetailResponse exercise
) {
}
