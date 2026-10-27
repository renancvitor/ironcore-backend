package com.ironcore.domain.workoutsession.workoutsession.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;

import java.util.List;
import java.util.Optional;

public interface WorkoutSessionRepository {
    WorkoutSession save(WorkoutSession session);
    Optional<WorkoutSession> findByIdAndPersonId(WorkoutSessionId id, PersonId personId);
    Optional<WorkoutSession> findInProgressByPersonId(PersonId personId);

    // A null cursor starts a new scan; only owners of IN_PROGRESS sessions are returned.
    List<PersonId> findInProgressPersonIdsAfter(PersonId afterPersonId, int limit);
}
