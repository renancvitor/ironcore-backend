package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.create;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import io.swagger.v3.oas.annotations.media.Schema;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

import java.time.LocalDateTime;

public record CreateWorkoutCycleResponse(
        Long id,
        Long personId,
        String name,
        Long trainingGoalId,
        Integer desiredDurationMonths,
        @Schema(description = "Dia inicial da semana para apresentação no frontend.", example = "MONDAY", requiredMode = Schema.RequiredMode.REQUIRED)
        WeekDay weekStartsOn,
        WorkoutStatus workoutStatus,
        WorkoutOrigin workoutOrigin,
        String notes,
        LocalDateTime createdAt
) {
}
