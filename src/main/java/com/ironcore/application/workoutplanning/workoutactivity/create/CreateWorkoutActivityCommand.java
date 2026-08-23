package com.ironcore.application.workoutplanning.workoutactivity.create;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

import java.math.BigDecimal;

public record CreateWorkoutActivityCommand(
        UserId actorUserId,
        WorkoutDayId workoutDayId,
        ExerciseId exerciseId,
        Integer sets,
        Integer repRangeMin,
        Integer repRangeMax,
        BigDecimal targetLoadKg,
        String targetLoadText,
        Integer durationMinutes,
        BigDecimal distanceKm,
        String intensityText,
        Integer restSeconds,
        String notes
) {
}
