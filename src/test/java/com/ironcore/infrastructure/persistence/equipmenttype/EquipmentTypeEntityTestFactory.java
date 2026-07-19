package com.ironcore.infrastructure.persistence.equipmenttype;

import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;

public final class EquipmentTypeEntityTestFactory {

    private EquipmentTypeEntityTestFactory() {
    }

    public static EquipmentTypeEntity equipmentTypeEntity() {
        return equipmentTypeEntity(1L);
    }

    public static EquipmentTypeEntity invalidEquipmentTypeEntity() {
        return equipmentTypeEntity(null);
    }

    private static EquipmentTypeEntity equipmentTypeEntity(Long id) {
        return new EquipmentTypeEntity(
                id,
                "CABLE",
                "Cabo",
                true,
                50
        );
    }
}
