package com.ironcore.infrastructure.persistence.exercise.catalog.repository;

import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.EquipmentTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.MuscleGroupItemResult;
import com.ironcore.application.exercise.catalog.usecase.MuscleSubgroupItemResult;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.activitytype.repository.ActivityTypeJpaRepository;
import com.ironcore.infrastructure.persistence.equipmenttype.repository.EquipmentTypeJpaRepository;
import com.ironcore.infrastructure.persistence.muscle.musclegroup.repository.MuscleGroupJpaRepository;
import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.repository.MuscleSubgroupJpaRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.ironcore.infrastructure.persistence.activitytype.ActivityTypeEntityTestFactory.activityTypeEntity;
import static com.ironcore.infrastructure.persistence.activitytype.ActivityTypeEntityTestFactory.invalidActivityTypeEntity;
import static com.ironcore.infrastructure.persistence.equipmenttype.EquipmentTypeEntityTestFactory.equipmentTypeEntity;
import static com.ironcore.infrastructure.persistence.equipmenttype.EquipmentTypeEntityTestFactory.invalidEquipmentTypeEntity;
import static com.ironcore.infrastructure.persistence.muscle.musclegroup.MuscleGroupEntityTestFactory.invalidMuscleGroupEntity;
import static com.ironcore.infrastructure.persistence.muscle.musclegroup.MuscleGroupEntityTestFactory.muscleGroupEntity;
import static com.ironcore.infrastructure.persistence.muscle.musclesubgroup.MuscleSubgroupEntityTestFactory.invalidMuscleSubgroupEntity;
import static com.ironcore.infrastructure.persistence.muscle.musclesubgroup.MuscleSubgroupEntityTestFactory.muscleSubgroupEntity;
import static com.ironcore.infrastructure.persistence.muscle.musclesubgroup.MuscleSubgroupEntityTestFactory.secondaryMuscleSubgroupEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseFilterCatalogQueryAdapterTest {

    @Mock
    private ActivityTypeJpaRepository activityTypeJpaRepository;

    @Mock
    private EquipmentTypeJpaRepository equipmentTypeJpaRepository;

    @Mock
    private MuscleGroupJpaRepository muscleGroupJpaRepository;

    @Mock
    private MuscleSubgroupJpaRepository muscleSubgroupJpaRepository;

    @InjectMocks
    private ExerciseFilterCatalogQueryAdapter adapter;

    @Nested
    class FindActiveActivityTypes {

        @Test
        void shouldReturnProjectedActiveActivityTypes() {
            when(activityTypeJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenReturn(List.of(activityTypeEntity()));

            List<ActivityTypeItemResult> result = adapter.findActiveActivityTypes();

            verify(activityTypeJpaRepository).findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc();
            assertThat(result).containsExactly(new ActivityTypeItemResult(
                    new ActivityTypeId(1L),
                    new ActivityTypeCode("STRENGTH"),
                    "Força"
            ));
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(activityTypeJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(adapter::findActiveActivityTypes)
                    .withMessage("Falha ao buscar tipos de atividade ativos.")
                    .withCauseInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldWrapMappingFailure() {
            when(activityTypeJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenReturn(List.of(invalidActivityTypeEntity()));

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(adapter::findActiveActivityTypes)
                    .withMessage("Falha ao projetar tipos de atividade ativos.");
        }
    }

    @Nested
    class FindActiveEquipmentTypes {

        @Test
        void shouldReturnProjectedActiveEquipmentTypes() {
            when(equipmentTypeJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenReturn(List.of(equipmentTypeEntity()));

            List<EquipmentTypeItemResult> result = adapter.findActiveEquipmentTypes();

            verify(equipmentTypeJpaRepository).findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc();
            assertThat(result).containsExactly(new EquipmentTypeItemResult(
                    new EquipmentTypeId(1L),
                    new EquipmentTypeCode("CABLE"),
                    "Cabo"
            ));
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(equipmentTypeJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(adapter::findActiveEquipmentTypes)
                    .withMessage("Falha ao buscar tipos de equipamento ativos.")
                    .withCauseInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldWrapMappingFailure() {
            when(equipmentTypeJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenReturn(List.of(invalidEquipmentTypeEntity()));

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(adapter::findActiveEquipmentTypes)
                    .withMessage("Falha ao projetar tipos de equipamento ativos.");
        }
    }

    @Nested
    class FindActiveMuscleGroups {

        @Test
        void shouldReturnProjectedActiveMuscleGroups() {
            when(muscleGroupJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenReturn(List.of(muscleGroupEntity()));

            List<MuscleGroupItemResult> result = adapter.findActiveMuscleGroups();

            verify(muscleGroupJpaRepository).findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc();
            assertThat(result).containsExactly(new MuscleGroupItemResult(
                    new MuscleGroupId(1L),
                    new MuscleGroupCode("BACK"),
                    "Costas"
            ));
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(muscleGroupJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(adapter::findActiveMuscleGroups)
                    .withMessage("Falha ao buscar grupos musculares ativos.")
                    .withCauseInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldWrapMappingFailure() {
            when(muscleGroupJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenReturn(List.of(invalidMuscleGroupEntity()));

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(adapter::findActiveMuscleGroups)
                    .withMessage("Falha ao projetar grupos musculares ativos.");
        }
    }

    @Nested
    class FindActiveMuscleSubgroups {

        @Test
        void shouldReturnProjectedActiveMuscleSubgroups() {
            when(muscleSubgroupJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenReturn(List.of(muscleSubgroupEntity(), secondaryMuscleSubgroupEntity()));

            List<MuscleSubgroupItemResult> result = adapter.findActiveMuscleSubgroups();

            verify(muscleSubgroupJpaRepository).findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc();
            assertThat(result).containsExactly(
                    new MuscleSubgroupItemResult(
                            new MuscleSubgroupId(1L),
                            new MuscleSubgroupCode("DELTOID"),
                            new MuscleGroupId(1L),
                            "Deltoide"
                    ),
                    new MuscleSubgroupItemResult(
                            new MuscleSubgroupId(2L),
                            new MuscleSubgroupCode("LATISSIMUS_DORSI"),
                            new MuscleGroupId(1L),
                            "Latíssimo do dorso"
                    )
            );
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(muscleSubgroupJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(adapter::findActiveMuscleSubgroups)
                    .withMessage("Falha ao buscar subgrupos musculares ativos.")
                    .withCauseInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldWrapMappingFailure() {
            when(muscleSubgroupJpaRepository.findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc())
                    .thenReturn(List.of(invalidMuscleSubgroupEntity()));

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(adapter::findActiveMuscleSubgroups)
                    .withMessage("Falha ao projetar subgrupos musculares ativos.");
        }

        @Test
        void shouldReturnProjectedActiveMuscleSubgroupsByMuscleGroupId() {
            MuscleGroupId muscleGroupId = new MuscleGroupId(1L);
            when(muscleSubgroupJpaRepository
                    .findAllByMuscleGroup_IdAndActiveTrueOrderBySortOrderAscDisplayNameAsc(muscleGroupId.value()))
                    .thenReturn(List.of(muscleSubgroupEntity()));

            List<MuscleSubgroupItemResult> result =
                    adapter.findActiveMuscleSubgroupsByMuscleGroupId(muscleGroupId);

            verify(muscleSubgroupJpaRepository)
                    .findAllByMuscleGroup_IdAndActiveTrueOrderBySortOrderAscDisplayNameAsc(muscleGroupId.value());
            assertThat(result).containsExactly(new MuscleSubgroupItemResult(
                    new MuscleSubgroupId(1L),
                    new MuscleSubgroupCode("DELTOID"),
                    muscleGroupId,
                    "Deltoide"
            ));
        }

        @Test
        void shouldWrapFilteredRepositoryFailure() {
            MuscleGroupId muscleGroupId = new MuscleGroupId(1L);
            when(muscleSubgroupJpaRepository
                    .findAllByMuscleGroup_IdAndActiveTrueOrderBySortOrderAscDisplayNameAsc(muscleGroupId.value()))
                    .thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findActiveMuscleSubgroupsByMuscleGroupId(muscleGroupId))
                    .withMessage("Falha ao buscar subgrupos musculares ativos por grupo muscular.")
                    .withCauseInstanceOf(RuntimeException.class);
        }
    }
}
