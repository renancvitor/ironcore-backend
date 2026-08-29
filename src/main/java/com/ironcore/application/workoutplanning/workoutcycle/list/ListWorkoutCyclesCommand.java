package com.ironcore.application.workoutplanning.workoutcycle.list;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

import java.time.LocalDate;

public record ListWorkoutCyclesCommand(
        UserId actorUserId,
        WorkoutStatus workoutStatus,
        TrainingGoalId trainingGoalId,
        LocalDate startDate,
        LocalDate endDate,
        String name,
        int page,
        int size
) {
}
