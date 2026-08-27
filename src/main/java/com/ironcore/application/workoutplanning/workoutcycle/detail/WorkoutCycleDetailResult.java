package com.ironcore.application.workoutplanning.workoutcycle.detail;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

import java.time.LocalDate;
import java.util.List;

public record WorkoutCycleDetailResult(
        WorkoutCycleId id,
        String name,
        WorkoutStatus workoutStatus,
        TrainingGoalDetailResult trainingGoal,
        LocalDate startDate,
        LocalDate endDate,
        Integer desiredDurationMonths,
        String notes,
        List<WorkoutDayDetailResult> days
) {
}
