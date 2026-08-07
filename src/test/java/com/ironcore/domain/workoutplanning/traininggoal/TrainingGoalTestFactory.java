package com.ironcore.domain.workoutplanning.traininggoal;

import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalCode;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;

public final class TrainingGoalTestFactory {

    private TrainingGoalTestFactory() {
    }

    public static TrainingGoal restoreTrainingGoal() {
        return TrainingGoal.restore(
                new TrainingGoalId(1L),
                new TrainingGoalCode(" hypertrophy "),
                " Hipertrofia ",
                true,
                10
        );
    }

    public static TrainingGoalCode code(String value) {
        return new TrainingGoalCode(value);
    }
}
