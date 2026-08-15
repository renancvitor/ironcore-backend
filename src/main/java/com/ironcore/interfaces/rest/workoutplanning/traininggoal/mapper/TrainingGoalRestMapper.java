package com.ironcore.interfaces.rest.workoutplanning.traininggoal.mapper;

import com.ironcore.application.workoutplanning.traininggoal.usecase.TrainingGoalResult;
import com.ironcore.interfaces.rest.workoutplanning.traininggoal.dto.TrainingGoalResponse;

public final class TrainingGoalRestMapper {

    private TrainingGoalRestMapper() {
    }

    public static TrainingGoalResponse toResponse(TrainingGoalResult result) {
        return new TrainingGoalResponse(
                result.id().value(),
                result.code().value(),
                result.name()
        );
    }
}
