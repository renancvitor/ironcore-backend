package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.start;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

import java.time.LocalDate;

public record StartWorkoutCycleResponse(
        Long id,
        Long trainingGoalId,
        LocalDate startDate,
        WorkoutStatus workoutStatus
) {
}
