package com.ironcore.application.workoutplanning.workoutcycle;

import com.ironcore.application.workoutplanning.workoutcycle.complete.CompleteWorkoutCycleCommand;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

public final class CompleteWorkoutCycleUseCaseTestFactory {

    private CompleteWorkoutCycleUseCaseTestFactory() {
    }

    public static CompleteWorkoutCycleCommand validCommand() {
        return new CompleteWorkoutCycleCommand(
                new UserId(1L),
                new WorkoutCycleId(1L)
        );
    }
}
