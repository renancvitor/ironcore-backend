package com.ironcore.application.workoutplanning.workoutday.create;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;

public record CreateWorkoutDayCommand(
        UserId actorUserId,
        WorkoutCycleId workoutCycleId,
        WeekDay weekDay,
        String title
) {
}
