package com.ironcore.application.workoutplanning.workoutday.update;

import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

import java.time.LocalDateTime;

public record UpdateWorkoutDayResult(
        WorkoutDayId id,
        String title,
        LocalDateTime updatedAt
) {
}
