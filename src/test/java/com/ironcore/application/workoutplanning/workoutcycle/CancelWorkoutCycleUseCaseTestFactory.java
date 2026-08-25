package com.ironcore.application.workoutplanning.workoutcycle;

import com.ironcore.application.workoutplanning.workoutcycle.cancel.CancelWorkoutCycleCommand;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

public final class CancelWorkoutCycleUseCaseTestFactory {

    private CancelWorkoutCycleUseCaseTestFactory() {
    }

    public static CancelWorkoutCycleCommand validCommand() {
        return new CancelWorkoutCycleCommand(
                new UserId(1L),
                new WorkoutCycleId(1L)
        );
    }
}
