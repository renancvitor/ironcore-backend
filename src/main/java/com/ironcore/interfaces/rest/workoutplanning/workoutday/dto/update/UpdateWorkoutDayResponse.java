package com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.update;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;

import java.time.LocalDateTime;

public record UpdateWorkoutDayResponse(
        Long id,
        Long workoutCycleId,
        WeekDay weekDay,
        String title,
        Integer sortOrder,
        LocalDateTime updatedAt
) {
}
