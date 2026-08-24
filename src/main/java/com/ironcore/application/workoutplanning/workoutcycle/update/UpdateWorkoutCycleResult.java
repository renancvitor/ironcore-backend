package com.ironcore.application.workoutplanning.workoutcycle.update;

import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UpdateWorkoutCycleResult(
        WorkoutCycleId id,
        String name,
        TrainingGoalId trainingGoalId,
        LocalDate startDate,
        WorkoutStatus workoutStatus,
        WorkoutOrigin workoutOrigin,
        Integer desiredDurationMonths,
        String notes,
        LocalDateTime updatedAt
) {
}
