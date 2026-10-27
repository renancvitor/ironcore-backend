package com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject;

import com.ironcore.domain.activitytype.model.ActivityType;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutsession.workoutsessionactivity.exception.InvalidWorkoutSessionActivityException;

public record WorkoutSessionActivitySource(WorkoutActivity activity, Exercise exercise, ActivityType activityType) {
    public WorkoutSessionActivitySource {
        if (activity == null || exercise == null || activityType == null) {
            throw new InvalidWorkoutSessionActivityException("Origem da atividade é obrigatória.");
        }
    }
}
