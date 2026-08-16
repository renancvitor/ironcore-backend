package com.ironcore.application.workoutplanning.workoutcycle;

import com.ironcore.application.workoutplanning.workoutcycle.create.CreateWorkoutCycleCommand;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;

public final class CreateWorkoutCycleUseCaseTestFactory {

    private CreateWorkoutCycleUseCaseTestFactory() {
    }

    public static CreateWorkoutCycleCommand validCommand() {
        return new CreateWorkoutCycleCommand(
                new UserId(1L),
                "Ciclo de hipertrofia",
                new TrainingGoalId(1L),
                3,
                "Planejamento inicial."
        );
    }
}
