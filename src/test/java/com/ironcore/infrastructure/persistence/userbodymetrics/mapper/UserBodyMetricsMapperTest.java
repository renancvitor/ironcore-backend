package com.ironcore.infrastructure.persistence.userbodymetrics.mapper;

import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.userbodymetrics.entity.UserBodyMetricsEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.restoreBodyMetrics;
import static com.ironcore.infrastructure.persistence.user.UserEntityTestFactory.userEntity;
import static com.ironcore.infrastructure.persistence.userbodymetrics.UserBodyMetricsTestFactory.createUserBodyMetricsEntity;
import static org.assertj.core.api.Assertions.assertThat;

class UserBodyMetricsMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapSecurityAndStatusFields() {
            UserBodyMetrics userBodyMetrics = restoreBodyMetrics();
            UserEntity user = userEntity();

            UserBodyMetricsEntity entity = UserBodyMetricsMapper.toEntity(userBodyMetrics, user);

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getUser()).isSameAs(user);
            assertThat(entity.getMeasuredAt()).isEqualTo(userBodyMetrics.getMeasuredAt());
            assertThat(entity.getWeightKg()).isEqualTo(80.0);
            assertThat(entity.getHeightCm()).isEqualTo(180.0);
            assertThat(entity.getNeckCm()).isEqualTo(40.0);
            assertThat(entity.getWaistCm()).isEqualTo(85.0);
            assertThat(entity.getBmi()).isEqualTo(24.69);
            assertThat(entity.getBodyFatPercentage()).isEqualTo(18.0);
            assertThat(entity.getFatMassKg()).isEqualTo(14.4);
            assertThat(entity.getLeanMassKg()).isEqualTo(65.6);
            assertThat(entity.getUpdatedAt()).isEqualTo(userBodyMetrics.getUpdatedAt());
            assertThat(entity.getNotes()).isEqualTo("Medição restaurada para teste.");
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreSecurityAndStatusFields() {
            UserBodyMetricsEntity entity = createUserBodyMetricsEntity();

            UserBodyMetrics userBodyMetrics = UserBodyMetricsMapper.toDomain(entity);

            assertThat(userBodyMetrics.getId().value()).isEqualTo(1L);
            assertThat(userBodyMetrics.getUserId().value()).isEqualTo(entity.getUser().getId());
            assertThat(userBodyMetrics.getMeasuredAt()).isEqualTo(entity.getMeasuredAt());
            assertThat(userBodyMetrics.getWeight().value()).isEqualTo(65.0);
            assertThat(userBodyMetrics.getHeight().value()).isEqualTo(1.67);
            assertThat(userBodyMetrics.getCircumferences().neck().value()).isEqualTo(20.0);
            assertThat(userBodyMetrics.getCircumferences().chest().value()).isEqualTo(120.0);
            assertThat(userBodyMetrics.getCircumferences().shoulder().value()).isEqualTo(150.0);
            assertThat(userBodyMetrics.getCircumferences().arm().value()).isEqualTo(36.0);
            assertThat(userBodyMetrics.getCircumferences().forearm().value()).isEqualTo(20.0);
            assertThat(userBodyMetrics.getCircumferences().waist().value()).isEqualTo(60.0);
            assertThat(userBodyMetrics.getCircumferences().hip().value()).isEqualTo(60.0);
            assertThat(userBodyMetrics.getCircumferences().thigh().value()).isEqualTo(20.0);
            assertThat(userBodyMetrics.getCircumferences().calf().value()).isEqualTo(20.0);
            assertThat(userBodyMetrics.getBmi().value()).isEqualTo(50.0);
            assertThat(userBodyMetrics.getBodyFatPercentage().value()).isEqualTo(10.0);
            assertThat(userBodyMetrics.getFatMassKg().value()).isEqualTo(10.0);
            assertThat(userBodyMetrics.getLeanMassKg().value()).isEqualTo(55.0);
            assertThat(userBodyMetrics.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
            assertThat(userBodyMetrics.getNotes()).isEqualTo("TEXT");
        }
    }
}
