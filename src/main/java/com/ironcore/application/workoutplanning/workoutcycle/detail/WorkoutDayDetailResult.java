package com.ironcore.application.workoutplanning.workoutcycle.detail;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

import java.util.List;

public record WorkoutDayDetailResult(
        WorkoutDayId id,
        WeekDay weekDay,
        String title,
        Integer sortOrder,
        List<WorkoutActivityDetailResult> activities
) {
}
