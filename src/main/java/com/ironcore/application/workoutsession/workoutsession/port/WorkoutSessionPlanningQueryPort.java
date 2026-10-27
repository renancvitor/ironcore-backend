package com.ironcore.application.workoutsession.workoutsession.port;

import com.ironcore.application.workoutsession.workoutsession.persistence.WorkoutSessionPlanningProjection;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

import java.util.Optional;

public interface WorkoutSessionPlanningQueryPort {

    Optional<WorkoutSessionPlanningProjection> lockByDayIdAndPersonId(WorkoutDayId dayId, PersonId personId);
}
