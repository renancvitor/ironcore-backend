package com.ironcore.application.workoutplanning.workoutday.create;

import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

import java.time.LocalDateTime;

public record CreateWorkoutDayResult(
        WorkoutDayId id,
        WorkoutCycleId workoutCycleId,
        WeekDay weekDay,
        String title,
        Integer sortOrder,
        LocalDateTime createdAt
) {
}
