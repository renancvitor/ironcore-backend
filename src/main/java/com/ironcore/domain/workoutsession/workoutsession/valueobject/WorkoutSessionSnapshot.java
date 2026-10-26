package com.ironcore.domain.workoutsession.workoutsession.valueobject;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;

public record WorkoutSessionSnapshot(String workoutCycleName, String workoutDayTitle, WeekDay weekDay) {
    public WorkoutSessionSnapshot {
        if (workoutCycleName == null || workoutCycleName.isBlank() || workoutCycleName.length() > 150
                || workoutDayTitle == null || workoutDayTitle.isBlank() || workoutDayTitle.length() > 100
                || weekDay == null) {
            throw new InvalidWorkoutSessionException("Snapshot da sessão inválido.");
        }
    }
}
