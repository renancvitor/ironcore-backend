package com.ironcore.domain.equipmenttype.model;

import com.ironcore.domain.equipmenttype.exception.InvalidEquipmentTypeException;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import lombok.Getter;

@Getter
public class EquipmentType {

    private final EquipmentTypeId id;
    private final EquipmentTypeCode code;
    private final String displayName;
    private final Boolean active;
    private final Integer sortOrder;

    private EquipmentType(EquipmentTypeId id, EquipmentTypeCode code, String displayName, Boolean active,
                          Integer sortOrder) {
        this.id = requireNonNull(id, "Id não pode ser nulo.");
        this.code = requireNonNull(code, "Código não pode ser nulo.");
        this.displayName = requireNonBlank(displayName, "Nome de exibição não pode ser nulo ou vazio.");
        this.active = requireNonNull(active, "Status ativo do tipo de equipamento não pode ser nulo.");
        this.sortOrder = requireNonNull(sortOrder, "Ordem de exibição do tipo de equipamento não pode ser nula.");
    }

    public static EquipmentType restore(EquipmentTypeId id, EquipmentTypeCode code, String displayName, Boolean active,
                                        Integer sortOrder) {
        return new EquipmentType(id, code, displayName, active, sortOrder);
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidEquipmentTypeException(message);
        }

        return value.trim();
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidEquipmentTypeException(message);
        }

        return value;
    }
}
