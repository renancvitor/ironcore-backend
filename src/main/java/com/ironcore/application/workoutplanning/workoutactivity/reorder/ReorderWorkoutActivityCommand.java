package com.ironcore.application.workoutplanning.workoutactivity.reorder;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;

public record ReorderWorkoutActivityCommand(
        UserId actorUserId,
        WorkoutActivityId id,
        Integer orderIndex
) {
}
