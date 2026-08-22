package com.ironcore.application.workoutplanning.workoutday.delete;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

public record DeleteWorkoutDayCommand(
        UserId actorUserId,
        WorkoutDayId id,
        WeekDay weekDay,
        String title,
        Integer sortOrder
) {
}
