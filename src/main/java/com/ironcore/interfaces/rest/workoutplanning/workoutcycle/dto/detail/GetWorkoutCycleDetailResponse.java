package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "Referência para ordenação circular no frontend. Os dias retornados mantém a ordem canônica por weekDay e sortOrder.", example = "MONDAY", requiredMode = Schema.RequiredMode.REQUIRED)
        WeekDay weekStartsOn,
        String notes,
        List<WorkoutDayDetailResponse> days
) {
}
