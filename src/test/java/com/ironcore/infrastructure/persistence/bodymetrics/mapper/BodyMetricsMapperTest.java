package com.ironcore.infrastructure.persistence.bodymetrics.mapper;

import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.bodymetrics.entity.BodyMetricsEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.restoreBodyMetrics;
import static com.ironcore.infrastructure.persistence.user.UserEntityTestFactory.userEntity;
import static com.ironcore.infrastructure.persistence.bodymetrics.BodyMetricsTestFactory.createUserBodyMetricsEntity;
import static org.assertj.core.api.Assertions.assertThat;

class BodyMetricsMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapSecurityAndStatusFields() {
            BodyMetrics bodyMetrics = restoreBodyMetrics();
            UserEntity user = userEntity();

            BodyMetricsEntity entity = BodyMetricsMapper.toEntity(bodyMetrics, user);

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getUser()).isSameAs(user);
            assertThat(entity.getMeasuredAt()).isEqualTo(bodyMetrics.getMeasuredAt());
            assertThat(entity.getWeightKg()).isEqualTo(80.0);
            assertThat(entity.getHeightCm()).isEqualTo(180.0);
            assertThat(entity.getNeckCm()).isEqualTo(40.0);
            assertThat(entity.getWaistCm()).isEqualTo(85.0);
            assertThat(entity.getBmi()).isEqualTo(24.69);
            assertThat(entity.getBodyFatPercentage()).isEqualTo(18.0);
            assertThat(entity.getFatMassKg()).isEqualTo(14.4);
            assertThat(entity.getLeanMassKg()).isEqualTo(65.6);
            assertThat(entity.getUpdatedAt()).isEqualTo(bodyMetrics.getUpdatedAt());
            assertThat(entity.getNotes()).isEqualTo("Medição restaurada para teste.");
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreSecurityAndStatusFields() {
            BodyMetricsEntity entity = createUserBodyMetricsEntity();

            BodyMetrics bodyMetrics = BodyMetricsMapper.toDomain(entity);

            assertThat(bodyMetrics.getId().value()).isEqualTo(1L);
            assertThat(bodyMetrics.getUserId().value()).isEqualTo(entity.getUser().getId());
            assertThat(bodyMetrics.getMeasuredAt()).isEqualTo(entity.getMeasuredAt());
            assertThat(bodyMetrics.getWeight().value()).isEqualTo(65.0);
            assertThat(bodyMetrics.getHeight().value()).isEqualTo(1.67);
            assertThat(bodyMetrics.getCircumferences().neck().value()).isEqualTo(20.0);
            assertThat(bodyMetrics.getCircumferences().chest().value()).isEqualTo(120.0);
            assertThat(bodyMetrics.getCircumferences().shoulder().value()).isEqualTo(150.0);
            assertThat(bodyMetrics.getCircumferences().arm().value()).isEqualTo(36.0);
            assertThat(bodyMetrics.getCircumferences().forearm().value()).isEqualTo(20.0);
            assertThat(bodyMetrics.getCircumferences().waist().value()).isEqualTo(60.0);
            assertThat(bodyMetrics.getCircumferences().hip().value()).isEqualTo(60.0);
            assertThat(bodyMetrics.getCircumferences().thigh().value()).isEqualTo(20.0);
            assertThat(bodyMetrics.getCircumferences().calf().value()).isEqualTo(20.0);
            assertThat(bodyMetrics.getBmi().value()).isEqualTo(50.0);
            assertThat(bodyMetrics.getBodyFatPercentage().value()).isEqualTo(10.0);
            assertThat(bodyMetrics.getFatMassKg().value()).isEqualTo(10.0);
            assertThat(bodyMetrics.getLeanMassKg().value()).isEqualTo(55.0);
            assertThat(bodyMetrics.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
            assertThat(bodyMetrics.getNotes()).isEqualTo("TEXT");
        }
    }
}
