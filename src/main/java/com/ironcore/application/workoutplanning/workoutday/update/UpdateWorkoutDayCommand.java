package com.ironcore.application.workoutplanning.workoutday.update;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

public record UpdateWorkoutDayCommand(
        UserId actorUserId,
        WorkoutDayId id,
        String title
) {
}
