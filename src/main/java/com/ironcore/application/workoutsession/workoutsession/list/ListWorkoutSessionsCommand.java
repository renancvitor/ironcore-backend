package com.ironcore.application.workoutsession.workoutsession.list;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import java.time.LocalDate;

public record ListWorkoutSessionsCommand(
        UserId actorUserId,
        WorkoutCycleId workoutCycleId,
        WorkoutDayId workoutDayId,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
) {
}
