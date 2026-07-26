package com.ironcore.application.exercise.catalog.usecase;

import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;

public record EquipmentTypeItemResult(
        EquipmentTypeId id,
        EquipmentTypeCode code,
        String name
) {
}
