package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.repository;

import com.ironcore.application.workoutplanning.workoutcycle.detail.WorkoutCycleDetailProjection;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutCycleDetailQueryAdapterTest {

    @Mock
    private WorkoutCycleDetailJpaRepository jpaRepository;

    private WorkoutCycleDetailQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WorkoutCycleDetailQueryAdapter(jpaRepository);
    }

    @Nested
    class SuccessfulFindDetail {

        @Test
        void shouldFindDetailByWorkoutCycleAndPersonIds() {
            WorkoutCycleId workoutCycleId = new WorkoutCycleId(10L);
            PersonId personId = new PersonId(20L);
            List<WorkoutCycleDetailProjection> expected = List.of();

            when(jpaRepository.findDetail(workoutCycleId.value(), personId.value())).thenReturn(expected);

            List<WorkoutCycleDetailProjection> result = adapter.findDetail(workoutCycleId, personId);

            assertThat(result).isSameAs(expected);
            verify(jpaRepository).findDetail(workoutCycleId.value(), personId.value());
        }
    }

    @Nested
    class PersistenceFailure {

        @Test
        void shouldWrapJpaRepositoryFailureInPersistenceException() {
            WorkoutCycleId workoutCycleId = new WorkoutCycleId(10L);
            PersonId personId = new PersonId(20L);

            when(jpaRepository.findDetail(workoutCycleId.value(), personId.value()))
                    .thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findDetail(workoutCycleId, personId))
                    .withMessage("Falha ao buscar detalhes do ciclo de treino.")
                    .withCauseInstanceOf(RuntimeException.class);

            verify(jpaRepository).findDetail(workoutCycleId.value(), personId.value());
        }
    }
}
