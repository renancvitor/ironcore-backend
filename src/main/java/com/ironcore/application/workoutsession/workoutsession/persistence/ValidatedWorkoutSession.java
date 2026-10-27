package com.ironcore.application.workoutsession.workoutsession.persistence;

import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import lombok.Getter;

@Getter
public final class ValidatedWorkoutSession {

    private final WorkoutSession session;

    ValidatedWorkoutSession(WorkoutSession session) {
        this.session = session;
    }
}
