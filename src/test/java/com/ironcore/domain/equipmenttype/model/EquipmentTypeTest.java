package com.ironcore.domain.equipmenttype.model;

import com.ironcore.domain.equipmenttype.exception.InvalidEquipmentTypeException;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.equipmenttype.EquipmentTypeTestFactory.restoreEquipmentType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EquipmentTypeTest {

    @Nested
    class Restoration {

        @Test
        void shouldRestoreExistingEquipmentType() {
            EquipmentType equipmentType = restoreEquipmentType();

            assertThat(equipmentType.getId()).isEqualTo(new EquipmentTypeId(1L));
            assertThat(equipmentType.getCode()).isEqualTo(new EquipmentTypeCode("CABLE"));
            assertThat(equipmentType.getDisplayName()).isEqualTo("Cabo");
            assertThat(equipmentType.getActive()).isTrue();
            assertThat(equipmentType.getSortOrder()).isEqualTo(50);
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRejectBlankDisplayName() {
            assertThatExceptionOfType(InvalidEquipmentTypeException.class)
                    .isThrownBy(() -> EquipmentType.restore(
                            new EquipmentTypeId(1L),
                            new EquipmentTypeCode("CaABLE"),
                            " ",
                            true,
                            50
                    ))
                    .withMessage("Nome de exibição não pode ser nulo ou vazio.");
        }

        @Test
        void shouldRequireIdWhenRestoring() {
            assertThatExceptionOfType(InvalidEquipmentTypeException.class)
                    .isThrownBy(() -> EquipmentType.restore(
                            null,
                            new EquipmentTypeCode("CABLE"),
                            "Cabo",
                            true,
                            50
                    ))
                    .withMessage("Id não pode ser nulo.");
        }
    }
}
