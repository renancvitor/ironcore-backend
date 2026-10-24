package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.update;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import io.swagger.v3.oas.annotations.media.Schema;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UpdateWorkoutCycleResponse(
        Long id,
        String name,
        Long trainingGoalId,
        LocalDate startDate,
        WorkoutStatus workoutStatus,
        WorkoutOrigin workoutOrigin,
        Integer desiredDurationMonths,
        @Schema(description = "Dia inicial da semana para apresentação no frontend.", example = "MONDAY", requiredMode = Schema.RequiredMode.REQUIRED)
        WeekDay weekStartsOn,
        String notes,
        LocalDateTime updatedAt
) {
}
