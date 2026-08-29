package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.list;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

import java.time.LocalDate;

public record ListWorkoutCyclesItemResponse(
        Long id,
        String name,
        WorkoutStatus workoutStatus,
        TrainingGoalItemResponse trainingGoal,
        LocalDate startDate,
        LocalDate endDate,
        Integer desiredDurationMonths
) {
}
