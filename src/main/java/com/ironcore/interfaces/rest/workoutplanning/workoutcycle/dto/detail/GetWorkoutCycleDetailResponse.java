package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

import java.time.LocalDate;
import java.util.List;

public record GetWorkoutCycleDetailResponse(
        Long id,
        String name,
        WorkoutStatus workoutStatus,
        TrainingGoalDetailResponse trainingGoal,
        LocalDate startDate,
        LocalDate endDate,
        Integer desiredDurationMonths,
        String notes,
        List<WorkoutDayDetailResponse> days
) {
}
