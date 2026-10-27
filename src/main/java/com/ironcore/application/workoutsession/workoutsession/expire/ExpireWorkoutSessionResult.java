package com.ironcore.application.workoutsession.workoutsession.expire;

import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;

import java.time.Instant;

public record ExpireWorkoutSessionResult(
        WorkoutSessionId id,
        Instant finishedAt,
        Instant updatedAt
) {
}
