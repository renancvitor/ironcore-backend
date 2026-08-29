package com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.create;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;

import java.time.LocalDateTime;

public record CreateWorkoutDayResponse(
        Long id,
        Long workoutCycleId,
        WeekDay weekDay,
        String title,
        Integer sortOrder,
        LocalDateTime createdAt
) {
}
