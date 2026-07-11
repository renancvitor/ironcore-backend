package com.ironcore.domain.equipmenttype;

import com.ironcore.domain.equipmenttype.model.EquipmentType;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;

public final class EquipmentTypeTestFactory {

    private EquipmentTypeTestFactory() {
    }

    public static EquipmentType restoreEquipmentType() {
        return EquipmentType.restore(
                new EquipmentTypeId(1L),
                new EquipmentTypeCode(" cable "),
                " Cabo ",
                true,
                50
        );
    }

    public static EquipmentTypeCode code(String value) {
        return new EquipmentTypeCode(value);
    }
}
