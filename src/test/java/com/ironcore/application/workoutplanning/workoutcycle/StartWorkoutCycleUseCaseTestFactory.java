package com.ironcore.application.workoutplanning.workoutcycle;

import com.ironcore.application.workoutplanning.workoutcycle.start.StartWorkoutCycleCommand;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

public final class StartWorkoutCycleUseCaseTestFactory {

    private StartWorkoutCycleUseCaseTestFactory() {
    }

    public static StartWorkoutCycleCommand validCommand() {
        return new StartWorkoutCycleCommand(
                new UserId(1L),
                new WorkoutCycleId(1L)
        );
    }
}
