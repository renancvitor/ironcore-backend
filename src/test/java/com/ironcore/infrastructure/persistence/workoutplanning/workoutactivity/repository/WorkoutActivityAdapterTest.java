package com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.repository;

import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.restoredWorkoutActivity;
import static com.ironcore.infrastructure.persistence.exercise.ExerciseEntityTestFactory.exerciseEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.WorkoutActivityEntityTestFactory.invalidWorkoutActivityEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.WorkoutActivityEntityTestFactory.workoutActivityEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutday.WorkoutDayEntityTestFactory.workoutDayEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.exercise.repository.ExerciseJpaRepository;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.entity.WorkoutActivityEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.repository.WorkoutDayJpaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkoutActivityAdapterTest {

    @Mock
    private WorkoutActivityJpaRepository workoutActivityJpaRepository;

    @Mock
    private WorkoutDayJpaRepository workoutDayJpaRepository;

    @Mock
    private ExerciseJpaRepository exerciseJpaRepository;

    @InjectMocks
    private WorkoutActivityAdapter adapter;

    @Nested
    class Save {

        @Test
        void shouldSaveWorkoutActivity() {
            when(workoutDayJpaRepository.getReferenceById(1L)).thenReturn(workoutDayEntity());
            when(exerciseJpaRepository.getReferenceById(1L)).thenReturn(exerciseEntity());
            when(workoutActivityJpaRepository.save(any(WorkoutActivityEntity.class)))
                    .thenReturn(workoutActivityEntity());

            WorkoutActivity result = adapter.save(restoredWorkoutActivity());

            assertThat(result.getId()).isEqualTo(new WorkoutActivityId(1L));
            assertThat(result.getWorkoutDayId()).isEqualTo(new WorkoutDayId(1L));
            assertThat(result.getExerciseId()).isEqualTo(new ExerciseId(1L));
            verify(workoutDayJpaRepository).getReferenceById(1L);
            verify(exerciseJpaRepository).getReferenceById(1L);
            verify(workoutActivityJpaRepository).save(any(WorkoutActivityEntity.class));
        }

        @Test
        void shouldWrapReferenceFailure() {
            when(workoutDayJpaRepository.getReferenceById(1L))
                    .thenThrow(new RuntimeException("workout day unavailable"));
            WorkoutActivity workoutActivity = restoredWorkoutActivity();

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.save(workoutActivity))
                    .withMessage("Falha ao obter referências para persistência do workout activity.");
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(workoutDayJpaRepository.getReferenceById(1L)).thenReturn(workoutDayEntity());
            when(exerciseJpaRepository.getReferenceById(1L)).thenReturn(exerciseEntity());
            when(workoutActivityJpaRepository.save(any(WorkoutActivityEntity.class)))
                    .thenThrow(new RuntimeException("database unavailable"));
            WorkoutActivity workoutActivity = restoredWorkoutActivity();

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.save(workoutActivity))
                    .withMessage("Falha ao persistir entidade.");
        }

        @Test
        void shouldWrapMappingFailureAfterPersistence() {
            when(workoutDayJpaRepository.getReferenceById(1L)).thenReturn(workoutDayEntity());
            when(exerciseJpaRepository.getReferenceById(1L)).thenReturn(exerciseEntity());
            when(workoutActivityJpaRepository.save(any(WorkoutActivityEntity.class)))
                    .thenReturn(invalidWorkoutActivityEntity());
            WorkoutActivity workoutActivity = restoredWorkoutActivity();

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.save(workoutActivity))
                    .withMessage("Falha ao converter persistido para domain.");
        }
    }

    @Nested
    class FindByIdAndPersonId {

        @Test
        void shouldFindWorkoutActivityByIdAndPersonId() {
            when(workoutActivityJpaRepository.findByIdAndWorkoutDay_WorkoutCycle_Person_Id(1L, 1L))
                    .thenReturn(Optional.of(workoutActivityEntity()));

            Optional<WorkoutActivity> result =
                    adapter.findByIdAndPersonId(new WorkoutActivityId(1L), new PersonId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new WorkoutActivityId(1L));
            assertThat(result.get().getWorkoutDayId()).isEqualTo(new WorkoutDayId(1L));
            assertThat(result.get().getExerciseId()).isEqualTo(new ExerciseId(1L));
        }

        @Test
        void shouldReturnEmptyWhenWorkoutActivityDoesNotBelongToPerson() {
            when(workoutActivityJpaRepository.findByIdAndWorkoutDay_WorkoutCycle_Person_Id(1L, 99L))
                    .thenReturn(Optional.empty());

            Optional<WorkoutActivity> result =
                    adapter.findByIdAndPersonId(new WorkoutActivityId(1L), new PersonId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(workoutActivityJpaRepository.findByIdAndWorkoutDay_WorkoutCycle_Person_Id(1L, 1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            WorkoutActivityId workoutActivityId = new WorkoutActivityId(1L);
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByIdAndPersonId(workoutActivityId, personId))
                    .withMessage("Falha ao buscar workout activity por id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(workoutActivityJpaRepository.findByIdAndWorkoutDay_WorkoutCycle_Person_Id(1L, 1L))
                    .thenReturn(Optional.of(invalidWorkoutActivityEntity()));
            WorkoutActivityId workoutActivityId = new WorkoutActivityId(1L);
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findByIdAndPersonId(workoutActivityId, personId))
                    .withMessage("Falha ao converter workout activity por id para domínio.");
        }
    }

    @Nested
    class FindByPersonIdAndWorkoutDayId {

        @Test
        void shouldFindWorkoutActivitiesByPersonIdAndWorkoutDayId() {
            when(workoutActivityJpaRepository
                            .findByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdOrderByOrderIndexAsc(1L, 1L))
                    .thenReturn(List.of(workoutActivityEntity()));

            List<WorkoutActivity> result =
                    adapter.findByPersonIdAndWorkoutDayId(new PersonId(1L), new WorkoutDayId(1L));

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getWorkoutDayId()).isEqualTo(new WorkoutDayId(1L));
            verify(workoutActivityJpaRepository)
                    .findByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdOrderByOrderIndexAsc(1L, 1L);
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(workoutActivityJpaRepository
                            .findByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdOrderByOrderIndexAsc(1L, 1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            PersonId personId = new PersonId(1L);
            WorkoutDayId workoutDayId = new WorkoutDayId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByPersonIdAndWorkoutDayId(personId, workoutDayId))
                    .withMessage("Falha ao buscar workout activities por person id e workout day id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(workoutActivityJpaRepository
                            .findByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdOrderByOrderIndexAsc(1L, 1L))
                    .thenReturn(List.of(invalidWorkoutActivityEntity()));
            PersonId personId = new PersonId(1L);
            WorkoutDayId workoutDayId = new WorkoutDayId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findByPersonIdAndWorkoutDayId(personId, workoutDayId))
                    .withMessage(
                            "Falha ao converter workout activities por person id e workout day id para domínio.");
        }
    }

    @Nested
    class ExistsByPersonIdAndWorkoutDayIdAndExerciseId {

        @Test
        void shouldReturnTrueWhenWorkoutActivityExists() {
            when(workoutActivityJpaRepository
                            .existsByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdAndExercise_Id(1L, 1L, 1L))
                    .thenReturn(true);

            boolean result =
                    adapter.existsByPersonIdAndWorkoutDayIdAndExerciseId(
                            new PersonId(1L), new WorkoutDayId(1L), new ExerciseId(1L));

            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalseWhenWorkoutActivityDoesNotExist() {
            when(workoutActivityJpaRepository
                            .existsByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdAndExercise_Id(1L, 1L, 99L))
                    .thenReturn(false);

            boolean result =
                    adapter.existsByPersonIdAndWorkoutDayIdAndExerciseId(
                            new PersonId(1L), new WorkoutDayId(1L), new ExerciseId(99L));

            assertThat(result).isFalse();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(workoutActivityJpaRepository
                            .existsByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdAndExercise_Id(1L, 1L, 1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            PersonId personId = new PersonId(1L);
            WorkoutDayId workoutDayId = new WorkoutDayId(1L);
            ExerciseId exerciseId = new ExerciseId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(
                            () ->
                                    adapter.existsByPersonIdAndWorkoutDayIdAndExerciseId(
                                            personId, workoutDayId, exerciseId))
                    .withMessage(
                            "Falha ao verificar existência de workout activity por person id, "
                                    + "workout day id e exercise id.");
        }
    }

    @Nested
    class ExistsByPersonIdAndWorkoutDayIdAndExerciseIdExcludingId {

        @Test
        void shouldReturnTrueWhenAnotherWorkoutActivityUsesExercise() {
            when(workoutActivityJpaRepository
                            .existsByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdAndExercise_IdAndIdNot(
                                    1L, 1L, 1L, 2L))
                    .thenReturn(true);

            boolean result =
                    adapter.existsByPersonIdAndWorkoutDayIdAndExerciseIdExcludingId(
                            new PersonId(1L),
                            new WorkoutDayId(1L),
                            new ExerciseId(1L),
                            new WorkoutActivityId(2L));

            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalseWhenNoOtherWorkoutActivityUsesExercise() {
            when(workoutActivityJpaRepository
                            .existsByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdAndExercise_IdAndIdNot(
                                    1L, 1L, 1L, 2L))
                    .thenReturn(false);

            boolean result =
                    adapter.existsByPersonIdAndWorkoutDayIdAndExerciseIdExcludingId(
                            new PersonId(1L),
                            new WorkoutDayId(1L),
                            new ExerciseId(1L),
                            new WorkoutActivityId(2L));

            assertThat(result).isFalse();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(workoutActivityJpaRepository
                            .existsByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdAndExercise_IdAndIdNot(
                                    1L, 1L, 1L, 2L))
                    .thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(
                            () ->
                                    adapter.existsByPersonIdAndWorkoutDayIdAndExerciseIdExcludingId(
                                            new PersonId(1L),
                                            new WorkoutDayId(1L),
                                            new ExerciseId(1L),
                                            new WorkoutActivityId(2L)))
                    .withMessage(
                            "Falha ao verificar existência de workout activity por person id, "
                                    + "workout day id e exercise id e pelo próprio id.");
        }
    }

    @Nested
    class DeleteById {

        @Test
        void shouldDeleteWorkoutActivityById() {
            WorkoutActivityId workoutActivityId = new WorkoutActivityId(1L);

            adapter.deleteById(workoutActivityId);

            verify(workoutActivityJpaRepository).deleteById(1L);
        }

        @Test
        void shouldWrapRepositoryFailure() {
            doThrow(new RuntimeException("database unavailable"))
                    .when(workoutActivityJpaRepository)
                    .deleteById(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.deleteById(new WorkoutActivityId(1L)))
                    .withMessage("Falha ao excluir atividade de treino por id.");
        }
    }
}
