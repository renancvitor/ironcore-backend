package com.ironcore.application.workoutplanning.workoutcycle.port;

import com.ironcore.application.workoutplanning.workoutcycle.detail.WorkoutCycleDetailProjection;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

import java.util.List;

public interface WorkoutCycleDetailQueryPort {

    List<WorkoutCycleDetailProjection> findDetail(
            WorkoutCycleId workoutCycleId,
            PersonId personId
    );
}
