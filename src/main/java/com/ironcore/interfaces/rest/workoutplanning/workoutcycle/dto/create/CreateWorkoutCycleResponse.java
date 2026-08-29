package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.create;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

import java.time.LocalDateTime;

public record CreateWorkoutCycleResponse(
        Long id,
        Long personId,
        String name,
        Long trainingGoalId,
        Integer desiredDurationMonths,
        WorkoutStatus workoutStatus,
        WorkoutOrigin workoutOrigin,
        String notes,
        LocalDateTime createdAt
) {
}
