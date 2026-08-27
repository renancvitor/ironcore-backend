package com.ironcore.application.workoutplanning.workoutcycle.detail;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WorkoutCycleDetailProjection(
        Long cycleId,
        String cycleName,
        WorkoutStatus workoutStatus,
        LocalDate startDate,
        LocalDate endDate,
        Integer desiredDurationMonths,
        String cycleNotes,

        Long trainingGoalId,
        String trainingGoalName,

        Long dayId,
        WeekDay weekDay,
        String dayTitle,
        Integer sortOrder,

        Long activityId,
        Integer orderIndex,
        Integer sets,
        Integer repRangeMin,
        Integer repRangeMax,
        BigDecimal targetLoadKg,
        String targetLoadText,
        Integer durationMinutes,
        BigDecimal distanceKm,
        String intensityText,
        Integer restSeconds,
        String activityNotes,

        Long exerciseId,
        String exerciseName,

        Long muscleGroupId,
        String muscleGroupName
) {
}
