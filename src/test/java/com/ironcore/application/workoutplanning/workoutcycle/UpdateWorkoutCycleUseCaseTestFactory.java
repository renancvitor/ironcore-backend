package com.ironcore.application.workoutplanning.workoutcycle;

import com.ironcore.application.workoutplanning.workoutcycle.update.UpdateWorkoutCycleCommand;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

public final class UpdateWorkoutCycleUseCaseTestFactory {

    private UpdateWorkoutCycleUseCaseTestFactory() {
    }

    public static UpdateWorkoutCycleCommand validCommand() {
        return new UpdateWorkoutCycleCommand(
                new UserId(1L),
                new WorkoutCycleId(1L),
                "Ciclo de força atualizado",
                new TrainingGoalId(2L),
                6,
                "Planejamento atualizado."
        );
    }
}
