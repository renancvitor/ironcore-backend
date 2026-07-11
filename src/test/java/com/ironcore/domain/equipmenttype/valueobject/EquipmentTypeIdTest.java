package com.ironcore.domain.equipmenttype.valueobject;

import com.ironcore.domain.equipmenttype.exception.InvalidEquipmentTypeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EquipmentTypeIdTest {

    @Test
    void shouldFailWhenEquipmentTypeIdIsNull() {
        assertThatThrownBy(() -> new EquipmentTypeId(null))
                .isInstanceOf(InvalidEquipmentTypeException.class);
    }

    @Test
    void shouldFailWhenEquipmentTypeIdIsZero() {
        assertThatThrownBy(() -> new EquipmentTypeId(0L))
                .isInstanceOf(InvalidEquipmentTypeException.class);
    }

    @Test
    void shouldFailWhenEquipmentTypeIdIsNegative() {
        assertThatThrownBy(() -> new EquipmentTypeId(-1L))
                .isInstanceOf(InvalidEquipmentTypeException.class);
    }
}
