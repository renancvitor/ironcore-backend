package com.ironcore.infrastructure.persistence.muscle.musclegroup.mapper;

import com.ironcore.domain.muscle.musclegroup.model.MuscleGroup;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.infrastructure.persistence.muscle.musclegroup.entity.MuscleGroupEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.muscle.musclegroup.MuscleGroupTestFactory.restoreMuscleGroup;
import static com.ironcore.infrastructure.persistence.muscle.musclegroup.MuscleGroupEntityTestFactory.muscleGroupEntity;
import static org.assertj.core.api.Assertions.assertThat;

class MuscleGroupMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapCatalogFields() {
            MuscleGroup muscleGroup = restoreMuscleGroup();

            MuscleGroupEntity entity = MuscleGroupMapper.toEntity(muscleGroup);

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getCode()).isEqualTo("BACK");
            assertThat(entity.getDisplayName()).isEqualTo("Costas");
            assertThat(entity.getActive()).isTrue();
            assertThat(entity.getSortOrder()).isEqualTo(20);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreCatalogFields() {
            MuscleGroupEntity entity = muscleGroupEntity();

            MuscleGroup muscleGroup = MuscleGroupMapper.toDomain(entity);

            assertThat(muscleGroup.getId()).isEqualTo(new MuscleGroupId(1L));
            assertThat(muscleGroup.getCode()).isEqualTo(new MuscleGroupCode("BACK"));
            assertThat(muscleGroup.getDisplayName()).isEqualTo("Costas");
            assertThat(muscleGroup.getActive()).isTrue();
            assertThat(muscleGroup.getSortOrder()).isEqualTo(20);
        }
    }
}
