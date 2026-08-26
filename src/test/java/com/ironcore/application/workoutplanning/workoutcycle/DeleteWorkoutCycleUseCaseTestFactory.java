package com.ironcore.application.workoutplanning.workoutcycle;

import com.ironcore.application.workoutplanning.workoutcycle.delete.DeleteWorkoutCycleCommand;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

public final class DeleteWorkoutCycleUseCaseTestFactory {

    private DeleteWorkoutCycleUseCaseTestFactory() {
    }

    public static DeleteWorkoutCycleCommand validCommand() {
        return new DeleteWorkoutCycleCommand(
                new UserId(1L),
                new WorkoutCycleId(1L)
        );
    }
}
