package com.ironcore.infrastructure.persistence.equipmenttype.repository;

import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentTypeJpaRepository extends JpaRepository<EquipmentTypeEntity, Long> {

    Optional<EquipmentTypeEntity> findByCode(String code);
}
