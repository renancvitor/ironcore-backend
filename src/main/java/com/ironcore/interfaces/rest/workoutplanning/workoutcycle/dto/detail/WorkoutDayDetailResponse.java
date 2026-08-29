package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;

import java.util.List;

public record WorkoutDayDetailResponse(
        Long id,
        WeekDay weekDay,
        String title,
        Integer sortOrder,
        List<WorkoutActivityDetailResponse> activities
) {
}
