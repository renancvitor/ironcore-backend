package com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.update;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateWorkoutActivityResponse(
        Long id,
        Long workoutDayId,
        Long exerciseId,
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
        LocalDateTime updatedAt
) {
}
