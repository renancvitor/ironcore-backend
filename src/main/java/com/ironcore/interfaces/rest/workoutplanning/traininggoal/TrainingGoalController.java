package com.ironcore.interfaces.rest.workoutplanning.traininggoal;

import com.ironcore.application.workoutplanning.traininggoal.usecase.ListTrainingGoalsUseCase;
import com.ironcore.interfaces.rest.workoutplanning.traininggoal.api.TrainingGoalApi;
import com.ironcore.interfaces.rest.workoutplanning.traininggoal.dto.TrainingGoalResponse;
import com.ironcore.interfaces.rest.workoutplanning.traininggoal.mapper.TrainingGoalRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/training-goals")
public class TrainingGoalController implements TrainingGoalApi {

    private final ListTrainingGoalsUseCase listTrainingGoalsUseCase;

    @GetMapping
    public ResponseEntity<List<TrainingGoalResponse>> listTrainingGoals() {
        List<TrainingGoalResponse> response = listTrainingGoalsUseCase.execute().stream()
                .map(TrainingGoalRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}
