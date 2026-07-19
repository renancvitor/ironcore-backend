package com.ironcore.infrastructure.persistence.equipmenttype.mapper;

import com.ironcore.domain.equipmenttype.model.EquipmentType;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.equipmenttype.EquipmentTypeTestFactory.restoreEquipmentType;
import static com.ironcore.infrastructure.persistence.equipmenttype.EquipmentTypeEntityTestFactory.equipmentTypeEntity;
import static org.assertj.core.api.Assertions.assertThat;

class EquipmentTypeMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapCatalogFields() {
            EquipmentType equipmentType = restoreEquipmentType();

            EquipmentTypeEntity entity = EquipmentTypeMapper.toEntity(equipmentType);

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getCode()).isEqualTo("CABLE");
            assertThat(entity.getDisplayName()).isEqualTo("Cabo");
            assertThat(entity.getActive()).isTrue();
            assertThat(entity.getSortOrder()).isEqualTo(50);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreCatalogFields() {
            EquipmentTypeEntity entity = equipmentTypeEntity();

            EquipmentType equipmentType = EquipmentTypeMapper.toDomain(entity);

            assertThat(equipmentType.getId()).isEqualTo(new EquipmentTypeId(1L));
            assertThat(equipmentType.getCode()).isEqualTo(new EquipmentTypeCode("CABLE"));
            assertThat(equipmentType.getDisplayName()).isEqualTo("Cabo");
            assertThat(equipmentType.getActive()).isTrue();
            assertThat(equipmentType.getSortOrder()).isEqualTo(50);
        }
    }
}
