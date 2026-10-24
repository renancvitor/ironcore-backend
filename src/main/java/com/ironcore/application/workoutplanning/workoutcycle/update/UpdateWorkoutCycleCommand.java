package com.ironcore.application.workoutplanning.workoutcycle.update;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

public record UpdateWorkoutCycleCommand(
        UserId actorUserId,
        WorkoutCycleId id,
        String name,
        TrainingGoalId trainingGoalId,
        Integer desiredDurationMonths,
        WeekDay weekStartsOn,
        String notes
) {
}
