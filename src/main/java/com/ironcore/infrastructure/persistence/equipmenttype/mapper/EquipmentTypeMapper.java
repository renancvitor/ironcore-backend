package com.ironcore.infrastructure.persistence.equipmenttype.mapper;

import com.ironcore.domain.equipmenttype.model.EquipmentType;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;

public class EquipmentTypeMapper {

    public static EquipmentTypeEntity toEntity(EquipmentType equipmentType) {
        try {
            return new EquipmentTypeEntity(
                    equipmentType.getId() == null ? null : equipmentType.getId().value(),
                    equipmentType.getCode() == null ? null : equipmentType.getCode().value(),
                    equipmentType.getDisplayName(),
                    equipmentType.getActive(),
                    equipmentType.getSortOrder()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter tipo de equipamento de domínio para entidade.", exception);
        }
    }

    public static EquipmentType toDomain(EquipmentTypeEntity equipmentTypeEntity) {
        try {
            return new EquipmentType(
                    new EquipmentTypeId(equipmentTypeEntity.getId()),
                    new EquipmentTypeCode(equipmentTypeEntity.getCode()),
                    equipmentTypeEntity.getDisplayName(),
                    equipmentTypeEntity.getActive(),
                    equipmentTypeEntity.getSortOrder()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter tipo de equipamento de entidade para domínio.", exception);
        }
    }
}
