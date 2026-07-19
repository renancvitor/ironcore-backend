package com.ironcore.domain.equipmenttype.repository;

import com.ironcore.domain.equipmenttype.model.EquipmentType;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;

import java.util.List;
import java.util.Optional;

public interface EquipmentTypeRepository {

    Optional<EquipmentType> findById(EquipmentTypeId id);

    Optional<EquipmentType> findByCode(EquipmentTypeCode code);

    List<EquipmentType> findAll();
}
