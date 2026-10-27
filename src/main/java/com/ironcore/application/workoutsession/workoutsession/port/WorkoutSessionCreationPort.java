package com.ironcore.application.workoutsession.workoutsession.port;

import com.ironcore.application.workoutsession.workoutsession.persistence.ValidatedWorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;

public interface WorkoutSessionCreationPort {

    WorkoutSession insert(ValidatedWorkoutSession validatedSession);
}
