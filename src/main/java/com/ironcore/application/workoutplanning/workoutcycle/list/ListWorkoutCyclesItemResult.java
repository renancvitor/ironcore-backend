package com.ironcore.application.workoutplanning.workoutcycle.list;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

import java.time.LocalDate;

public record ListWorkoutCyclesItemResult(
        WorkoutCycleId id,
        String name,
        WorkoutStatus workoutStatus,
        TrainingGoalItemResult trainingGoal,
        LocalDate startDate,
        LocalDate endDate,
        Integer desiredDurationMonths
) {
}
