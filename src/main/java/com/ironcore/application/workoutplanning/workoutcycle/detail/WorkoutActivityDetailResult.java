package com.ironcore.application.workoutplanning.workoutcycle.detail;

import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;

import java.math.BigDecimal;

public record WorkoutActivityDetailResult(
        WorkoutActivityId id,
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
        ExerciseDetailResult exercise
) {
}
