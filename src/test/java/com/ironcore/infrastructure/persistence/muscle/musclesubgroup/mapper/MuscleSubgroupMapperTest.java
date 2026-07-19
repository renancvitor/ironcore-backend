package com.ironcore.infrastructure.persistence.muscle.musclesubgroup.mapper;

import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.model.MuscleSubgroup;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.entity.MuscleSubgroupEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.muscle.musclesubgroup.MuscleSubgroupTestFactory.restoreMuscleSubgroup;
import static com.ironcore.infrastructure.persistence.muscle.musclegroup.MuscleGroupEntityTestFactory.muscleGroupEntity;
import static com.ironcore.infrastructure.persistence.muscle.musclesubgroup.MuscleSubgroupEntityTestFactory.muscleSubgroupEntity;
import static org.assertj.core.api.Assertions.assertThat;

class MuscleSubgroupMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapCatalogFieldsAndMuscleGroupReference() {
            MuscleSubgroup muscleSubgroup = restoreMuscleSubgroup();

            MuscleSubgroupEntity entity = MuscleSubgroupMapper.toEntity(muscleSubgroup, muscleGroupEntity());

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getMuscleGroup().getId()).isEqualTo(1L);
            assertThat(entity.getCode()).isEqualTo("DELTOID");
            assertThat(entity.getDisplayName()).isEqualTo("Deltoide");
            assertThat(entity.getActive()).isTrue();
            assertThat(entity.getSortOrder()).isEqualTo(10);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreCatalogFieldsAndMuscleGroupId() {
            MuscleSubgroupEntity entity = muscleSubgroupEntity();

            MuscleSubgroup muscleSubgroup = MuscleSubgroupMapper.toDomain(entity);

            assertThat(muscleSubgroup.getId()).isEqualTo(new MuscleSubgroupId(1L));
            assertThat(muscleSubgroup.getMuscleGroupId()).isEqualTo(new MuscleGroupId(1L));
            assertThat(muscleSubgroup.getCode()).isEqualTo(new MuscleSubgroupCode("DELTOID"));
            assertThat(muscleSubgroup.getDisplayName()).isEqualTo("Deltoide");
            assertThat(muscleSubgroup.getActive()).isTrue();
            assertThat(muscleSubgroup.getSortOrder()).isEqualTo(10);
        }
    }
}
