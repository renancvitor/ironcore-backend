package com.ironcore.infrastructure.persistence.workoutplanning.workoutday.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.repository.WorkoutCycleJpaRepository;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity.WorkoutDayEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.restoredWorkoutDay;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.WorkoutCycleEntityTestFactory.workoutCycleEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutday.WorkoutDayEntityTestFactory.invalidWorkoutDayEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutday.WorkoutDayEntityTestFactory.workoutDayEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutDayAdapterTest {

    @Mock
    private WorkoutDayJpaRepository workoutDayJpaRepository;

    @Mock
    private WorkoutCycleJpaRepository workoutCycleJpaRepository;

    @InjectMocks
    private WorkoutDayAdapter adapter;

    @Nested
    class Save {

        @Test
        void shouldSaveWorkoutDay() {
            when(workoutCycleJpaRepository.getReferenceById(1L)).thenReturn(workoutCycleEntity());
            when(workoutDayJpaRepository.save(any(WorkoutDayEntity.class))).thenReturn(workoutDayEntity());

            WorkoutDay result = adapter.save(restoredWorkoutDay());

            assertThat(result.getId()).isEqualTo(new WorkoutDayId(1L));
            assertThat(result.getWorkoutCycleId()).isEqualTo(new WorkoutCycleId(1L));
            verify(workoutCycleJpaRepository).getReferenceById(1L);
            verify(workoutDayJpaRepository).save(any(WorkoutDayEntity.class));
        }

        @Test
        void shouldWrapReferenceFailure() {
            when(workoutCycleJpaRepository.getReferenceById(1L))
                    .thenThrow(new RuntimeException("workout cycle unavailable"));
            WorkoutDay workoutDay = restoredWorkoutDay();

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.save(workoutDay))
                    .withMessage("Falha ao obter referência para persistência do workout day.");
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(workoutCycleJpaRepository.getReferenceById(1L)).thenReturn(workoutCycleEntity());
            when(workoutDayJpaRepository.save(any(WorkoutDayEntity.class)))
                    .thenThrow(new RuntimeException("database unavailable"));
            WorkoutDay workoutDay = restoredWorkoutDay();

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.save(workoutDay))
                    .withMessage("Falha ao persistir entidade.");
        }

        @Test
        void shouldWrapMappingFailureAfterPersistence() {
            when(workoutCycleJpaRepository.getReferenceById(1L)).thenReturn(workoutCycleEntity());
            when(workoutDayJpaRepository.save(any(WorkoutDayEntity.class)))
                    .thenReturn(invalidWorkoutDayEntity());
            WorkoutDay workoutDay = restoredWorkoutDay();

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.save(workoutDay))
                    .withMessage("Falha ao converter persistido para domain.");
        }
    }

    @Nested
    class FindByIdAndPersonId {

        @Test
        void shouldFindWorkoutDayByIdAndPersonId() {
            when(workoutDayJpaRepository.findByIdAndWorkoutCycle_Person_Id(1L, 1L))
                    .thenReturn(Optional.of(workoutDayEntity()));

            Optional<WorkoutDay> result = adapter.findByIdAndPersonId(
                    new WorkoutDayId(1L),
                    new PersonId(1L)
            );

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new WorkoutDayId(1L));
            assertThat(result.get().getWorkoutCycleId()).isEqualTo(new WorkoutCycleId(1L));
        }

        @Test
        void shouldReturnEmptyWhenWorkoutDayDoesNotBelongToPerson() {
            when(workoutDayJpaRepository.findByIdAndWorkoutCycle_Person_Id(1L, 99L))
                    .thenReturn(Optional.empty());

            Optional<WorkoutDay> result = adapter.findByIdAndPersonId(
                    new WorkoutDayId(1L),
                    new PersonId(99L)
            );

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(workoutDayJpaRepository.findByIdAndWorkoutCycle_Person_Id(1L, 1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            WorkoutDayId workoutDayId = new WorkoutDayId(1L);
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByIdAndPersonId(workoutDayId, personId))
                    .withMessage("Falha ao buscar workout day por id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(workoutDayJpaRepository.findByIdAndWorkoutCycle_Person_Id(1L, 1L))
                    .thenReturn(Optional.of(invalidWorkoutDayEntity()));
            WorkoutDayId workoutDayId = new WorkoutDayId(1L);
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findByIdAndPersonId(workoutDayId, personId))
                    .withMessage("Falha ao converter workout day por id para domínio.");
        }
    }

    @Nested
    class FindByWorkoutCycleId {

        @Test
        void shouldFindWorkoutDaysByWorkoutCycleId() {
            when(workoutDayJpaRepository.findByWorkoutCycle_IdOrderByOrderIndexAsc(1L))
                    .thenReturn(List.of(workoutDayEntity()));

            List<WorkoutDay> result = adapter.findByWorkoutCycleId(new WorkoutCycleId(1L));

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getWorkoutCycleId()).isEqualTo(new WorkoutCycleId(1L));
            verify(workoutDayJpaRepository).findByWorkoutCycle_IdOrderByOrderIndexAsc(1L);
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(workoutDayJpaRepository.findByWorkoutCycle_IdOrderByOrderIndexAsc(1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            WorkoutCycleId workoutCycleId = new WorkoutCycleId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByWorkoutCycleId(workoutCycleId))
                    .withMessage("Falha ao buscar workout days por workout cycle id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(workoutDayJpaRepository.findByWorkoutCycle_IdOrderByOrderIndexAsc(1L))
                    .thenReturn(List.of(invalidWorkoutDayEntity()));
            WorkoutCycleId workoutCycleId = new WorkoutCycleId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findByWorkoutCycleId(workoutCycleId))
                    .withMessage("Falha ao converter workout days por workout cycle id para domínio.");
        }
    }
}
