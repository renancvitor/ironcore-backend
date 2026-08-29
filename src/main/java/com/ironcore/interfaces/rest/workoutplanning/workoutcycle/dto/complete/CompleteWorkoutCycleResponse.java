package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.complete;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

import java.time.LocalDate;

public record CompleteWorkoutCycleResponse(
        Long id,
        Long trainingGoalId,
        LocalDate startDate,
        LocalDate endDate,
        WorkoutStatus workoutStatus
) {
}
