package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.repository;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.workoutplanning.workoutcycle.list.ListWorkoutCyclesItemResult;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.WorkoutCycleEntityTestFactory.invalidWorkoutCycleEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.WorkoutCycleEntityTestFactory.workoutCycleEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutCycleSearchAdapterTest {

    private static final Sort EXPECTED_SORT = Sort.by("name").ascending()
            .and(Sort.by("id").ascending());

    @Mock
    private WorkoutCycleJpaRepository workoutCycleJpaRepository;

    @InjectMocks
    private WorkoutCycleSearchAdapter adapter;

    @Test
    void shouldReturnProjectedPageAndForwardPagination() {
        PageRequest requestedPage = PageRequest.of(1, 2, EXPECTED_SORT);
        Page<WorkoutCycleEntity> entities = new PageImpl<>(
                List.of(workoutCycleEntity()),
                requestedPage,
                5
        );
        when(workoutCycleJpaRepository.findAll(
                ArgumentMatchers.<Specification<WorkoutCycleEntity>>any(),
                eq(requestedPage)
        )).thenReturn(entities);

        PageResult<ListWorkoutCyclesItemResult> result = adapter.findWorkoutCycles(
                new PersonId(1L),
                WorkoutStatus.IN_PROGRESS,
                new TrainingGoalId(1L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31),
                "hipertrofia",
                new PageQuery(1, 2)
        );

        verify(workoutCycleJpaRepository).findAll(
                ArgumentMatchers.<Specification<WorkoutCycleEntity>>any(),
                eq(requestedPage)
        );
        assertThat(result.content()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(new WorkoutCycleId(1L));
            assertThat(item.name()).isEqualTo("Ciclo de hipertrofia");
            assertThat(item.workoutStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
            assertThat(item.trainingGoal().id()).isEqualTo(new TrainingGoalId(1L));
            assertThat(item.trainingGoal().name()).isEqualTo("Hipertrofia");
            assertThat(item.desiredDurationMonths()).isEqualTo(3);
        });
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.last()).isFalse();
    }

    @Test
    void shouldWrapRepositoryFailure() {
        PageRequest requestedPage = PageRequest.of(0, 10, EXPECTED_SORT);
        when(workoutCycleJpaRepository.findAll(
                ArgumentMatchers.<Specification<WorkoutCycleEntity>>any(),
                eq(requestedPage)
        )).thenThrow(new RuntimeException("database unavailable"));

        assertThatExceptionOfType(PersistenceException.class)
                .isThrownBy(() -> adapter.findWorkoutCycles(
                        new PersonId(1L),
                        null,
                        null,
                        null,
                        null,
                        null,
                        new PageQuery(0, 10)
                ))
                .withMessage("Falha ao buscar ciclos.")
                .withCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldWrapMappingFailure() {
        PageRequest requestedPage = PageRequest.of(0, 10, EXPECTED_SORT);
        when(workoutCycleJpaRepository.findAll(
                ArgumentMatchers.<Specification<WorkoutCycleEntity>>any(),
                eq(requestedPage)
        )).thenReturn(new PageImpl<>(List.of(invalidWorkoutCycleEntity()), requestedPage, 1));

        assertThatExceptionOfType(DataMappingException.class)
                .isThrownBy(() -> adapter.findWorkoutCycles(
                        new PersonId(1L),
                        null,
                        null,
                        null,
                        null,
                        null,
                        new PageQuery(0, 10)
                ))
                .withMessage("Falha ao projetar ciclos.");
    }
}
