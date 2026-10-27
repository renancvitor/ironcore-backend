package com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.start;

import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;

import java.time.Instant;
import java.time.LocalDate;

public record StartWorkoutSessionResponse(
        Long id,
        Long workoutCycleId,
        Long workoutDayId,
        LocalDate performedOn,
        Instant startedAt,
        WorkoutSessionStatus status,
        Instant createdAt
) {
}
