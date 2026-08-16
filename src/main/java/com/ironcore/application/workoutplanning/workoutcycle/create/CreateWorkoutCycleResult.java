package com.ironcore.application.workoutplanning.workoutcycle.create;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

import java.time.LocalDateTime;

public record CreateWorkoutCycleResult(
        WorkoutCycleId id,
        PersonId personId,
        String name,
        TrainingGoalId trainingGoalId,
        Integer desiredDurationMonths,
        WorkoutStatus workoutStatus,
        WorkoutOrigin workoutOrigin,
        String notes,
        LocalDateTime createdAt
) {
}
