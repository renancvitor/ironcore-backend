package com.ironcore.infrastructure.persistence.equipmenttype.repository;

import com.ironcore.domain.equipmenttype.model.EquipmentType;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
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

import static com.ironcore.infrastructure.persistence.equipmenttype.EquipmentTypeEntityTestFactory.equipmentTypeEntity;
import static com.ironcore.infrastructure.persistence.equipmenttype.EquipmentTypeEntityTestFactory.invalidEquipmentTypeEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentTypeAdapterTest {

    @Mock
    private EquipmentTypeJpaRepository equipmentTypeJpaRepository;

    @InjectMocks
    private EquipmentTypeAdapter adapter;

    @Nested
    class FindById {

        @Test
        void shouldFindEquipmentTypeById() {
            when(equipmentTypeJpaRepository.findById(1L)).thenReturn(Optional.of(equipmentTypeEntity()));

            Optional<EquipmentType> result = adapter.findById(new EquipmentTypeId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new EquipmentTypeId(1L));
            assertThat(result.get().getCode()).isEqualTo(new EquipmentTypeCode("CABLE"));
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(equipmentTypeJpaRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<EquipmentType> result = adapter.findById(new EquipmentTypeId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(equipmentTypeJpaRepository.findById(1L)).thenThrow(new RuntimeException("database unavailable"));
            EquipmentTypeId equipmentTypeId = new EquipmentTypeId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findById(equipmentTypeId))
                    .withMessage("Falha ao buscar equipment type por id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(equipmentTypeJpaRepository.findById(1L)).thenReturn(Optional.of(invalidEquipmentTypeEntity()));
            EquipmentTypeId equipmentTypeId = new EquipmentTypeId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findById(equipmentTypeId))
                    .withMessage("Falha ao converter tipo de equipamento de domínio para entidade.");
        }
    }

    @Nested
    class FindByCode {

        @Test
        void shouldFindEquipmentTypeByCode() {
            when(equipmentTypeJpaRepository.findByCode("CABLE")).thenReturn(Optional.of(equipmentTypeEntity()));

            Optional<EquipmentType> result = adapter.findByCode(new EquipmentTypeCode(" cable "));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new EquipmentTypeId(1L));
            assertThat(result.get().getCode()).isEqualTo(new EquipmentTypeCode("CABLE"));
        }

        @Test
        void shouldReturnEmptyWhenCodeDoesNotExist() {
            when(equipmentTypeJpaRepository.findByCode("MISSING")).thenReturn(Optional.empty());

            Optional<EquipmentType> result = adapter.findByCode(new EquipmentTypeCode("missing"));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(equipmentTypeJpaRepository.findByCode("CABLE"))
                    .thenThrow(new RuntimeException("database unavailable"));
            EquipmentTypeCode code = new EquipmentTypeCode("CABLE");

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByCode(code))
                    .withMessage("Falha ao buscar equipment type por code.");
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldFindAllEquipmentTypes() {
            when(equipmentTypeJpaRepository.findAll()).thenReturn(List.of(equipmentTypeEntity()));

            List<EquipmentType> result = adapter.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getId()).isEqualTo(new EquipmentTypeId(1L));
            assertThat(result.getFirst().getCode()).isEqualTo(new EquipmentTypeCode("CABLE"));
        }

        @Test
        void shouldReturnEmptyWhenNoEquipmentTypesExist() {
            when(equipmentTypeJpaRepository.findAll()).thenReturn(List.of());

            List<EquipmentType> result = adapter.findAll();

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(equipmentTypeJpaRepository.findAll()).thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findAll())
                    .withMessage("Falha ao buscar equipment types.");
        }
    }
}
