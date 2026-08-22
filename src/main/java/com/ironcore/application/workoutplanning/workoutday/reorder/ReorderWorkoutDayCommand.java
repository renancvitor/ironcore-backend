package com.ironcore.application.workoutplanning.workoutday.reorder;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

public record ReorderWorkoutDayCommand(
        UserId actorUserId,
        WorkoutDayId id,
        WeekDay weekDay,
        Integer sortOrder
) {
}
