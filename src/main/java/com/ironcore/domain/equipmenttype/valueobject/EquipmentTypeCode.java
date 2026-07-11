package com.ironcore.domain.equipmenttype.valueobject;

import com.ironcore.domain.equipmenttype.exception.InvalidEquipmentTypeException;

public record EquipmentTypeCode(String value) {

    public EquipmentTypeCode {
        if (value == null || value.isBlank()) {
            throw new InvalidEquipmentTypeException("Código do tipo de equipamento é obrigatório.");
        }

        value = value.trim().toUpperCase();

        if (!value.matches("[A-Z0-9_]+")) {
            throw new InvalidEquipmentTypeException("Código do tipo de equipamento deve conter apenas letras " +
                    "maiúsculas números e underscores.");
        }

        if (value.length() > 50) {
            throw new InvalidEquipmentTypeException("Código do tipo de equipamento não pode exceder 50 caracteres.");
        }
    }
}
