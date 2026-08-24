package com.ironcore.application.workoutplanning.workoutactivity.create;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateWorkoutActivityResult(
        WorkoutActivityId id,
        WorkoutDayId workoutDayId,
        ExerciseId exerciseId,
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
        LocalDateTime createdAt
) {
}
