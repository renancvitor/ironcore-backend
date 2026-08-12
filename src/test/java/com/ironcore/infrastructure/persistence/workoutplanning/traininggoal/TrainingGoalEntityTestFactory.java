package com.ironcore.infrastructure.persistence.workoutplanning.traininggoal;

import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.entity.TrainingGoalEntity;

public final class TrainingGoalEntityTestFactory {

    private TrainingGoalEntityTestFactory() {
    }

    public static TrainingGoalEntity trainingGoalEntity() {
        return trainingGoalEntity(1L);
    }

    public static TrainingGoalEntity invalidTrainingGoalEntity() {
        return trainingGoalEntity(null);
    }

    private static TrainingGoalEntity trainingGoalEntity(Long id) {
        return new TrainingGoalEntity(
                id,
                "HYPERTROPHY",
                "Hipertrofia",
                true,
                10
        );
    }
}
