package com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.close;

import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;

import java.time.Instant;
import java.time.LocalDate;

public record CloseWorkoutSessionResponse(
        Long id,
        LocalDate performedOn,
        Instant startedAt,
        Instant finishedAt,
        WorkoutSessionStatus status,
        WorkoutSessionCloseReason closeReason,
        Instant updatedAt
) {
}
