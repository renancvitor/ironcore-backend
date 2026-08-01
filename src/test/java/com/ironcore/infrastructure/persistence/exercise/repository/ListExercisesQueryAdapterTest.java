package com.ironcore.infrastructure.persistence.exercise.repository;

import com.ironcore.application.exercise.usecase.ListExercisesItemResult;
import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static com.ironcore.infrastructure.persistence.exercise.ExerciseEntityTestFactory.exerciseEntity;
import static com.ironcore.infrastructure.persistence.exercise.ExerciseEntityTestFactory.invalidExerciseEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListExercisesQueryAdapterTest {

    private static final Sort EXPECTED_SORT = Sort.by("name").ascending()
            .and(Sort.by("id").ascending());

    @Mock
    private ExerciseJpaRepository exerciseJpaRepository;

    @InjectMocks
    private ListExercisesQueryAdapter adapter;

    @Test
    void shouldReturnProjectedPageAndForwardPagination() {
        PageRequest requestedPage = PageRequest.of(1, 2, EXPECTED_SORT);
        Page<ExerciseEntity> entities = new PageImpl<>(
                List.of(exerciseEntity()),
                requestedPage,
                5
        );
        when(exerciseJpaRepository.findAll(
                ArgumentMatchers.<Specification<ExerciseEntity>>any(),
                eq(requestedPage)
        ))
                .thenReturn(entities);

        PageResult<ListExercisesItemResult> result = adapter.findActiveExercises(
                "supino",
                new ActivityTypeId(1L),
                new EquipmentTypeId(2L),
                new MuscleGroupId(3L),
                new MuscleSubgroupId(4L),
                TargetRoleType.PRIMARY,
                new PageQuery(1, 2)
        );

        verify(exerciseJpaRepository).findAll(
                ArgumentMatchers.<Specification<ExerciseEntity>>any(),
                eq(requestedPage)
        );
        assertThat(result.content()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(new ExerciseId(1L));
            assertThat(item.name()).isEqualTo("Supino reto");
            assertThat(item.equipmentType().id()).isEqualTo(new EquipmentTypeId(1L));
            assertThat(item.equipmentType().code()).isEqualTo(new EquipmentTypeCode("CABLE"));
            assertThat(item.equipmentType().name()).isEqualTo("Cabo");
            assertThat(item.activityType().id()).isEqualTo(new ActivityTypeId(1L));
            assertThat(item.activityType().code()).isEqualTo(new ActivityTypeCode("STRENGTH"));
            assertThat(item.activityType().name()).isEqualTo("Força");
            assertThat(item.unilateral()).isFalse();
            assertThat(item.compound()).isTrue();
            assertThat(item.suggestedRestSeconds()).isEqualTo(90);
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
        when(exerciseJpaRepository.findAll(
                ArgumentMatchers.<Specification<ExerciseEntity>>any(),
                eq(requestedPage)
        ))
                .thenThrow(new RuntimeException("database unavailable"));

        assertThatExceptionOfType(PersistenceException.class)
                .isThrownBy(() -> adapter.findActiveExercises(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new PageQuery(0, 10)
                ))
                .withMessage("Falha ao buscar exercícios.")
                .withCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldWrapMappingFailure() {
        PageRequest requestedPage = PageRequest.of(0, 10, EXPECTED_SORT);
        when(exerciseJpaRepository.findAll(
                ArgumentMatchers.<Specification<ExerciseEntity>>any(),
                eq(requestedPage)
        ))
                .thenReturn(new PageImpl<>(List.of(invalidExerciseEntity()), requestedPage, 1));

        assertThatExceptionOfType(DataMappingException.class)
                .isThrownBy(() -> adapter.findActiveExercises(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new PageQuery(0, 10)
                ))
                .withMessage("Falha ao projetar exercícios.");
    }
}
