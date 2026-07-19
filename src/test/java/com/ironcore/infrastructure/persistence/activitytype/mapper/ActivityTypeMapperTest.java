package com.ironcore.infrastructure.persistence.activitytype.mapper;

import com.ironcore.domain.activitytype.model.ActivityType;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.activitytype.ActivityTypeTestFactory.restoreActivityType;
import static com.ironcore.infrastructure.persistence.activitytype.ActivityTypeEntityTestFactory.activityTypeEntity;
import static org.assertj.core.api.Assertions.assertThat;

class ActivityTypeMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapCatalogFields() {
            ActivityType activityType = restoreActivityType();

            ActivityTypeEntity entity = ActivityTypeMapper.toEntity(activityType);

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getCode()).isEqualTo("STRENGTH");
            assertThat(entity.getDisplayName()).isEqualTo("Força");
            assertThat(entity.getActive()).isTrue();
            assertThat(entity.getSortOrder()).isEqualTo(10);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreCatalogFields() {
            ActivityTypeEntity entity = activityTypeEntity();

            ActivityType activityType = ActivityTypeMapper.toDomain(entity);

            assertThat(activityType.getId()).isEqualTo(new ActivityTypeId(1L));
            assertThat(activityType.getCode()).isEqualTo(new ActivityTypeCode("STRENGTH"));
            assertThat(activityType.getDisplayName()).isEqualTo("Força");
            assertThat(activityType.getActive()).isTrue();
            assertThat(activityType.getSortOrder()).isEqualTo(10);
        }
    }
}
