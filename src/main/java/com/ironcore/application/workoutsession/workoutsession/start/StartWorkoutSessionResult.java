package com.ironcore.application.workoutsession.workoutsession.start;

import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;

import java.time.Instant;
import java.time.LocalDate;

public record StartWorkoutSessionResult(
        WorkoutSessionId id,
        WorkoutCycleId workoutCycleId,
        WorkoutDayId workoutDayId,
        LocalDate performedOn,
        Instant startedAt,
        WorkoutSessionStatus status,
        Instant createdAt
) {
}
