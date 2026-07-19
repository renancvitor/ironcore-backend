package com.ironcore.infrastructure.persistence.equipmenttype.repository;

import com.ironcore.domain.equipmenttype.model.EquipmentType;
import com.ironcore.domain.equipmenttype.repository.EquipmentTypeRepository;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;
import com.ironcore.infrastructure.persistence.equipmenttype.mapper.EquipmentTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EquipmentTypeAdapter implements EquipmentTypeRepository {

    private final EquipmentTypeJpaRepository equipmentTypeJpaRepository;

    @Override
    public Optional<EquipmentType> findById(EquipmentTypeId id) {
        Optional<EquipmentTypeEntity> entity;
        try {
            Long equipmentTypeId = Objects.requireNonNull(
                    id.value(),
                    "Id do tipo de equipamento não pode ser nulo."
            );
            entity = equipmentTypeJpaRepository.findById(equipmentTypeId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar equipment type por id.", exception);
        }

        try {
            return entity.map(EquipmentTypeMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter tipo de equipamento de domínio para entidade.", exception);
        }
    }

    @Override
    public Optional<EquipmentType> findByCode(EquipmentTypeCode code) {
        Optional<EquipmentTypeEntity> entity;
        try {
            String equipmentTypeCode = Objects.requireNonNull(
                    code.value(),
                    "Code do tipo de equipamento não pode ser nulo."
            );
            entity = equipmentTypeJpaRepository.findByCode(equipmentTypeCode);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar equipment type por code.", exception);
        }

        try {
            return entity.map(EquipmentTypeMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter tipo de equipamento de domínio para entidade.", exception);
        }
    }

    @Override
    public List<EquipmentType> findAll() {
        List<EquipmentTypeEntity> entities;
        try {
            entities = equipmentTypeJpaRepository.findAll();
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar equipment types.", exception);
        }

        try {
            return entities.stream().map(EquipmentTypeMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter equipment types.", exception);
        }
    }
}
