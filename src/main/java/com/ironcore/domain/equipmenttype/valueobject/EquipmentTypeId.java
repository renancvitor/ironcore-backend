package com.ironcore.domain.equipmenttype.valueobject;

import com.ironcore.domain.equipmenttype.exception.InvalidEquipmentTypeException;

public record EquipmentTypeId(Long value) {

    public EquipmentTypeId {

        if (value == null || value <= 0) {
            throw new InvalidEquipmentTypeException("Id do tipo de equipamento deve ser positivo.");
        }
    }
}
