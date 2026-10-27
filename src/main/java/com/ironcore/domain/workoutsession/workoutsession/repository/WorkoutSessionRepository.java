package com.ironcore.domain.workoutsession.workoutsession.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;

import java.util.Optional;

public interface WorkoutSessionRepository {
    WorkoutSession save(WorkoutSession session);
    Optional<WorkoutSession> findByIdAndPersonId(WorkoutSessionId id, PersonId personId);
    Optional<WorkoutSession> findInProgressByPersonId(PersonId personId);
}
