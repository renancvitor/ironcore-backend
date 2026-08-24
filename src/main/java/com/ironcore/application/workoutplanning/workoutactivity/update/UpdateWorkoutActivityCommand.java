package com.ironcore.application.workoutplanning.workoutactivity.update;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;

import java.math.BigDecimal;

public record UpdateWorkoutActivityCommand(
        UserId actorUserId,
        WorkoutActivityId id,
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
