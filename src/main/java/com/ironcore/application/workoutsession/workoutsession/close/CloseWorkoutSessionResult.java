package com.ironcore.application.workoutsession.workoutsession.close;

import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;

import java.time.Instant;
import java.time.LocalDate;

public record CloseWorkoutSessionResult(
        WorkoutSessionId id,
        LocalDate performedOn,
        Instant startedAt,
        Instant finishedAt,
        WorkoutSessionStatus status,
        WorkoutSessionCloseReason closeReason,
        Instant updatedAt
) {
}
