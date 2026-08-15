package com.ironcore.application.workoutplanning.traininggoal.usecase;

import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.repository.TrainingGoalRepository;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalCode;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.ironcore.domain.workoutplanning.traininggoal.TrainingGoalTestFactory.restoreTrainingGoal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListTrainingGoalsUseCaseTest {

    @Mock
    private TrainingGoalRepository trainingGoalRepository;

    @InjectMocks
    private ListTrainingGoalsUseCase listTrainingGoalsUseCase;

    @Test
    void shouldListActiveTrainingGoals() {
        TrainingGoal trainingGoal = restoreTrainingGoal();
        when(trainingGoalRepository.findAllActive()).thenReturn(List.of(trainingGoal));

        List<TrainingGoalResult> result = listTrainingGoalsUseCase.execute();

        verify(trainingGoalRepository).findAllActive();
        assertThat(result).containsExactly(new TrainingGoalResult(
                new TrainingGoalId(1L),
                new TrainingGoalCode("HYPERTROPHY"),
                "Hipertrofia"
        ));
    }
}
