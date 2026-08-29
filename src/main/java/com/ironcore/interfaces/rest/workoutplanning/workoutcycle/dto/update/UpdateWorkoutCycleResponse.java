package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.update;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UpdateWorkoutCycleResponse(
        Long id,
        String name,
        Long trainingGoalId,
        LocalDate startDate,
        WorkoutStatus workoutStatus,
        WorkoutOrigin workoutOrigin,
        Integer desiredDurationMonths,
        String notes,
        LocalDateTime updatedAt
) {
}
