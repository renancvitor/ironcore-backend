package com.ironcore.infrastructure.persistence.userbodymetrics.mapper;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.*;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.userbodymetrics.entity.UserBodyMetricsEntity;

public class UserBodyMetricsMapper {

    public static UserBodyMetricsEntity toEntity(UserBodyMetrics userBodyMetrics, UserEntity user) {
        BodyCircumferences circumferences = userBodyMetrics.getCircumferences();

        try {
            return new UserBodyMetricsEntity(
                    userBodyMetrics.getId() == null ? null : userBodyMetrics.getId().value(),
                    user,
                    userBodyMetrics.getMeasuredAt(),
                    userBodyMetrics.getWeight().value(),
                    userBodyMetrics.getHeight().value(),
                    circumferences == null ? null : valueOf(circumferences.neck()),
                    circumferences == null ? null : valueOf(circumferences.chest()),
                    circumferences == null ? null : valueOf(circumferences.shoulder()),
                    circumferences == null ? null : valueOf(circumferences.arm()),
                    circumferences == null ? null : valueOf(circumferences.forearm()),
                    circumferences == null ? null : valueOf(circumferences.waist()),
                    circumferences == null ? null : valueOf(circumferences.hip()),
                    circumferences == null ? null : valueOf(circumferences.thigh()),
                    circumferences == null ? null : valueOf(circumferences.calf()),
                    userBodyMetrics.getBmi() == null ? null : userBodyMetrics.getBmi().value(),
                    userBodyMetrics.getBodyFatPercentage() == null ? null : userBodyMetrics.getBodyFatPercentage().value(),
                    userBodyMetrics.getFatMassKg() == null ? null : userBodyMetrics.getFatMassKg().value(),
                    userBodyMetrics.getLeanMassKg() == null ? null : userBodyMetrics.getLeanMassKg().value(),
                    userBodyMetrics.getUpdatedAt(),
                    userBodyMetrics.getNotes()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter domínio para entidade.", exception);
        }
    }

    public static UserBodyMetrics toDomain(UserBodyMetricsEntity entity) {
        try {
            return UserBodyMetrics.restore(
                    new UserBodyMetricsId(entity.getId()),
                    new UserId(entity.getUser().getId()),
                    entity.getMeasuredAt(),
                    new BodyWeightKg(entity.getWeightKg()),
                    new BodyHeightCm(entity.getHeightCm()),
                    new BodyCircumferences(
                            toBodyCircumferenceCm(entity.getNeckCm()),
                            toBodyCircumferenceCm(entity.getChestCm()),
                            toBodyCircumferenceCm(entity.getShoulderCm()),
                            toBodyCircumferenceCm(entity.getArmCm()),
                            toBodyCircumferenceCm(entity.getForearmCm()),
                            toBodyCircumferenceCm(entity.getWaistCm()),
                            toBodyCircumferenceCm(entity.getHipCm()),
                            toBodyCircumferenceCm(entity.getThighCm()),
                            toBodyCircumferenceCm(entity.getCalfCm())
                    ),
                    toBmi(entity.getBmi()),
                    toBodyFatPercentage(entity.getBodyFatPercentage()),
                    toFatMassKg(entity.getFatMassKg()),
                    toLeanMassKg(entity.getLeanMassKg()),
                    entity.getUpdatedAt(),
                    entity.getNotes()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter entidade para domínio.", exception);
        }
    }

    private static Double valueOf(BodyCircumferenceCm circumferenceCm) {
        return circumferenceCm == null ? null : circumferenceCm.value();
    }

    private static BodyCircumferenceCm toBodyCircumferenceCm(Double value) {
        return value == null ? null : new BodyCircumferenceCm(value);
    }

    private static BMI toBmi(Double value) {
        return value == null ? null : new BMI(value);
    }

    private static BodyFatPercentage toBodyFatPercentage(Double value) {
        return value == null ? null : new BodyFatPercentage(value);
    }

    private static FatMassKg toFatMassKg(Double value) {
        return value == null ? null : new FatMassKg(value);
    }

    private static LeanMassKg toLeanMassKg(Double value) {
        return value == null ? null : new LeanMassKg(value);
    }
}
