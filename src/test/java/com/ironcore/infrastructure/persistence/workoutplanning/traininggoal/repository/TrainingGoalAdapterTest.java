package com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.repository;

import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalCode;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.TrainingGoalEntityTestFactory.invalidTrainingGoalEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.TrainingGoalEntityTestFactory.trainingGoalEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingGoalAdapterTest {

    @Mock
    private TrainingGoalJpaRepository trainingGoalJpaRepository;

    @InjectMocks
    private TrainingGoalAdapter adapter;

    @Nested
    class FindById {

        @Test
        void shouldFindTrainingGoalById() {
            when(trainingGoalJpaRepository.findById(1L)).thenReturn(Optional.of(trainingGoalEntity()));

            Optional<TrainingGoal> result = adapter.findById(new TrainingGoalId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new TrainingGoalId(1L));
            assertThat(result.get().getCode()).isEqualTo(new TrainingGoalCode("HYPERTROPHY"));
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(trainingGoalJpaRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<TrainingGoal> result = adapter.findById(new TrainingGoalId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(trainingGoalJpaRepository.findById(1L)).thenThrow(new RuntimeException("database unavailable"));
            TrainingGoalId trainingGoalId = new TrainingGoalId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findById(trainingGoalId))
                    .withMessage("Falha ao buscar training goal por id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(trainingGoalJpaRepository.findById(1L)).thenReturn(Optional.of(invalidTrainingGoalEntity()));
            TrainingGoalId trainingGoalId = new TrainingGoalId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findById(trainingGoalId))
                    .withMessage("Falha ao converter training goal por id para domínio.");
        }
    }

    @Nested
    class FindAllActive {

        @Test
        void shouldReturnActiveTrainingGoals() {
            when(trainingGoalJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenReturn(List.of(trainingGoalEntity()));

            List<TrainingGoal> result = adapter.findAllActive();

            verify(trainingGoalJpaRepository).findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc();
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getId()).isEqualTo(new TrainingGoalId(1L));
            assertThat(result.getFirst().getCode()).isEqualTo(new TrainingGoalCode("HYPERTROPHY"));
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(trainingGoalJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(adapter::findAllActive)
                    .withMessage("Falha ao listar training goals ativos.")
                    .withCauseInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldWrapMappingFailure() {
            when(trainingGoalJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenReturn(List.of(invalidTrainingGoalEntity()));

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(adapter::findAllActive)
                    .withMessage("Falha ao converter lista de training goals ativos para domínio.");
        }
    }
}
