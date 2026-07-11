package com.ironcore.domain.equipmenttype.valueobject;

import com.ironcore.domain.equipmenttype.exception.InvalidEquipmentTypeException;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.equipmenttype.EquipmentTypeTestFactory.code;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EquipmentTypeCodeTest {

    @Test
    void shouldNormalizeCode() {
        EquipmentTypeCode code = code(" cable ");

        assertThat(code.value()).isEqualTo("CABLE");
    }

    @Test
    void shouldRejectNullCode() {
        assertThatThrownBy(() -> new EquipmentTypeCode(null))
                .isInstanceOf(InvalidEquipmentTypeException.class);
    }

    @Test
    void shouldRejectWhenCodeIsBlank() {
        assertThatThrownBy(() -> new EquipmentTypeCode(" "))
                .isInstanceOf(InvalidEquipmentTypeException.class);
    }

    @Test
    void shouldRejectInvalidCodeFormat() {
        assertThatThrownBy(() -> new EquipmentTypeCode("invalid-@"))
                .isInstanceOf(InvalidEquipmentTypeException.class);
    }

    @Test
    void shouldRejectLengthGreaterThan50() {
        assertThatThrownBy(() -> new EquipmentTypeCode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
                .isInstanceOf(InvalidEquipmentTypeException.class);
    }
}
