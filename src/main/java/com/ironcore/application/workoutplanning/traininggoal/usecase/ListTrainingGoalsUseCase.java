package com.ironcore.application.workoutplanning.traininggoal.usecase;

import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.repository.TrainingGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListTrainingGoalsUseCase {

    private final TrainingGoalRepository trainingGoalRepository;

    @Transactional(readOnly = true)
    public List<TrainingGoalResult> execute() {
        List<TrainingGoal> trainingGoals = trainingGoalRepository.findAllActive();

        return trainingGoals.stream()
                .map(trainingGoal -> new TrainingGoalResult(
                        trainingGoal.getId(),
                        trainingGoal.getCode(),
                        trainingGoal.getDisplayName())
                )
                .toList();
    }
}
