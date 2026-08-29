package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.cancel;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

public record CancelWorkoutCycleResponse(
        Long id,
        Long trainingGoalId,
        WorkoutStatus workoutStatus
) {
}
