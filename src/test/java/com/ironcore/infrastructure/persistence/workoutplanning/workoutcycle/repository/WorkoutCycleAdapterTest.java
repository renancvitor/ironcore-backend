package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.repository.TrainingGoalJpaRepository;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.infrastructure.persistence.person.PersonEntityTestFactory.personEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.TrainingGoalEntityTestFactory.trainingGoalEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.WorkoutCycleEntityTestFactory.invalidWorkoutCycleEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.WorkoutCycleEntityTestFactory.workoutCycleEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutCycleAdapterTest {

    @Mock
    private WorkoutCycleJpaRepository workoutCycleJpaRepository;

    @Mock
    private PersonJpaRepository personJpaRepository;

    @Mock
    private TrainingGoalJpaRepository trainingGoalJpaRepository;

    @InjectMocks
    private WorkoutCycleAdapter adapter;

    @Nested
    class Save {

        @Test
        void shouldSaveWorkoutCycle() {
            when(personJpaRepository.getReferenceById(1L)).thenReturn(personEntity());
            when(trainingGoalJpaRepository.getReferenceById(1L)).thenReturn(trainingGoalEntity());
            when(workoutCycleJpaRepository.save(any(WorkoutCycleEntity.class))).thenReturn(workoutCycleEntity());

            WorkoutCycle result = adapter.save(inProgressWorkoutCycle());

            assertThat(result.getId()).isEqualTo(new WorkoutCycleId(1L));
            assertThat(result.getPersonId()).isEqualTo(new PersonId(1L));
            assertThat(result.getTrainingGoalId()).isEqualTo(new TrainingGoalId(1L));
            verify(personJpaRepository).getReferenceById(1L);
            verify(trainingGoalJpaRepository).getReferenceById(1L);
            verify(workoutCycleJpaRepository).save(any(WorkoutCycleEntity.class));
        }

        @Test
        void shouldWrapReferenceFailure() {
            when(personJpaRepository.getReferenceById(1L))
                    .thenThrow(new RuntimeException("person unavailable"));
            WorkoutCycle workoutCycle = inProgressWorkoutCycle();

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.save(workoutCycle))
                    .withMessage("Falha ao obter referências para persistência do workout cycle.");
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(personJpaRepository.getReferenceById(1L)).thenReturn(personEntity());
            when(trainingGoalJpaRepository.getReferenceById(1L)).thenReturn(trainingGoalEntity());
            when(workoutCycleJpaRepository.save(any(WorkoutCycleEntity.class)))
                    .thenThrow(new RuntimeException("database unavailable"));
            WorkoutCycle workoutCycle = inProgressWorkoutCycle();

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.save(workoutCycle))
                    .withMessage("Falha ao persistir entidade.");
        }

        @Test
        void shouldWrapMappingFailureAfterPersistence() {
            when(personJpaRepository.getReferenceById(1L)).thenReturn(personEntity());
            when(trainingGoalJpaRepository.getReferenceById(1L)).thenReturn(trainingGoalEntity());
            when(workoutCycleJpaRepository.save(any(WorkoutCycleEntity.class)))
                    .thenReturn(invalidWorkoutCycleEntity());
            WorkoutCycle workoutCycle = inProgressWorkoutCycle();

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.save(workoutCycle))
                    .withMessage("Falha ao converter persistido para domain.");
        }
    }

    @Nested
    class FindByIdAndPersonId {

        @Test
        void shouldFindWorkoutCycleByIdAndPersonId() {
            when(workoutCycleJpaRepository.findByIdAndPerson_Id(1L, 1L))
                    .thenReturn(Optional.of(workoutCycleEntity()));

            Optional<WorkoutCycle> result = adapter.findByIdAndPersonId(
                    new WorkoutCycleId(1L),
                    new PersonId(1L)
            );

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new WorkoutCycleId(1L));
            assertThat(result.get().getPersonId()).isEqualTo(new PersonId(1L));
        }

        @Test
        void shouldReturnEmptyWhenWorkoutCycleDoesNotBelongToPerson() {
            when(workoutCycleJpaRepository.findByIdAndPerson_Id(1L, 99L)).thenReturn(Optional.empty());

            Optional<WorkoutCycle> result = adapter.findByIdAndPersonId(
                    new WorkoutCycleId(1L),
                    new PersonId(99L)
            );

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(workoutCycleJpaRepository.findByIdAndPerson_Id(1L, 1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            WorkoutCycleId workoutCycleId = new WorkoutCycleId(1L);
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByIdAndPersonId(workoutCycleId, personId))
                    .withMessage("Falha ao buscar workout cycle por id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(workoutCycleJpaRepository.findByIdAndPerson_Id(1L, 1L))
                    .thenReturn(Optional.of(invalidWorkoutCycleEntity()));
            WorkoutCycleId workoutCycleId = new WorkoutCycleId(1L);
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findByIdAndPersonId(workoutCycleId, personId))
                    .withMessage("Falha ao converter workout cycle por id para domínio.");
        }
    }

    @Nested
    class FindByPersonId {

        @Test
        void shouldFindWorkoutCyclesByPersonId() {
            when(workoutCycleJpaRepository.findByPerson_Id(1L)).thenReturn(List.of(workoutCycleEntity()));

            List<WorkoutCycle> result = adapter.findByPersonId(new PersonId(1L));

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getPersonId()).isEqualTo(new PersonId(1L));
            verify(workoutCycleJpaRepository).findByPerson_Id(1L);
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(workoutCycleJpaRepository.findByPerson_Id(1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByPersonId(personId))
                    .withMessage("Falha ao buscar workout cycle por person id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(workoutCycleJpaRepository.findByPerson_Id(1L))
                    .thenReturn(List.of(invalidWorkoutCycleEntity()));
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findByPersonId(personId))
                    .withMessage("Falha ao converter workout cycle por person id para domínio.");
        }
    }

    @Nested
    class FindByPersonIdAndWorkoutStatus {

        @Test
        void shouldFindWorkoutCyclesByPersonIdAndWorkoutStatus() {
            when(workoutCycleJpaRepository.findByPerson_IdAndWorkoutStatus(1L, WorkoutStatus.IN_PROGRESS))
                    .thenReturn(List.of(workoutCycleEntity()));

            List<WorkoutCycle> result = adapter.findByPersonIdAndWorkoutStatus(
                    new PersonId(1L),
                    WorkoutStatus.IN_PROGRESS
            );

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getWorkoutStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
            verify(workoutCycleJpaRepository)
                    .findByPerson_IdAndWorkoutStatus(1L, WorkoutStatus.IN_PROGRESS);
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(workoutCycleJpaRepository.findByPerson_IdAndWorkoutStatus(1L, WorkoutStatus.IN_PROGRESS))
                    .thenThrow(new RuntimeException("database unavailable"));
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByPersonIdAndWorkoutStatus(personId, WorkoutStatus.IN_PROGRESS))
                    .withMessage("Falha ao buscar workout cycle por person id e workout status.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(workoutCycleJpaRepository.findByPerson_IdAndWorkoutStatus(1L, WorkoutStatus.IN_PROGRESS))
                    .thenReturn(List.of(invalidWorkoutCycleEntity()));
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findByPersonIdAndWorkoutStatus(personId, WorkoutStatus.IN_PROGRESS))
                    .withMessage("Falha ao converter workout cycle por person id e workout status para domínio.");
        }
    }

    @Nested
    class FindByPersonIdAndTrainingGoalId {

        @Test
        void shouldFindWorkoutCyclesByPersonIdAndTrainingGoalId() {
            when(workoutCycleJpaRepository.findByPerson_IdAndTrainingGoal_Id(1L, 1L))
                    .thenReturn(List.of(workoutCycleEntity()));

            List<WorkoutCycle> result = adapter.findByPersonIdAndTrainingGoalId(
                    new PersonId(1L),
                    new TrainingGoalId(1L)
            );

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainingGoalId()).isEqualTo(new TrainingGoalId(1L));
            verify(workoutCycleJpaRepository).findByPerson_IdAndTrainingGoal_Id(1L, 1L);
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(workoutCycleJpaRepository.findByPerson_IdAndTrainingGoal_Id(1L, 1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            PersonId personId = new PersonId(1L);
            TrainingGoalId trainingGoalId = new TrainingGoalId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByPersonIdAndTrainingGoalId(personId, trainingGoalId))
                    .withMessage("Falha ao buscar workout cycle por person id e training goal id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(workoutCycleJpaRepository.findByPerson_IdAndTrainingGoal_Id(1L, 1L))
                    .thenReturn(List.of(invalidWorkoutCycleEntity()));
            PersonId personId = new PersonId(1L);
            TrainingGoalId trainingGoalId = new TrainingGoalId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findByPersonIdAndTrainingGoalId(personId, trainingGoalId))
                    .withMessage("Falha ao converter workout cycle por person id e training goal id para domínio.");
        }
    }
}
