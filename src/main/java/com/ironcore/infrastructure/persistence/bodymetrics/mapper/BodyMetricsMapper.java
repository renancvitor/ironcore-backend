package com.ironcore.infrastructure.persistence.bodymetrics.mapper;

import com.ironcore.domain.bodymetrics.valueobject.*;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.bodymetrics.entity.BodyMetricsEntity;

public class BodyMetricsMapper {

    public static BodyMetricsEntity toEntity(BodyMetrics bodyMetrics, UserEntity user) {
        BodyCircumferences circumferences = bodyMetrics.getCircumferences();

        try {
            return new BodyMetricsEntity(
                    bodyMetrics.getId() == null ? null : bodyMetrics.getId().value(),
                    user,
                    bodyMetrics.getMeasuredAt(),
                    bodyMetrics.getWeight().value(),
                    bodyMetrics.getHeight().value(),
                    circumferences == null ? null : valueOf(circumferences.neck()),
                    circumferences == null ? null : valueOf(circumferences.chest()),
                    circumferences == null ? null : valueOf(circumferences.shoulder()),
                    circumferences == null ? null : valueOf(circumferences.arm()),
                    circumferences == null ? null : valueOf(circumferences.forearm()),
                    circumferences == null ? null : valueOf(circumferences.waist()),
                    circumferences == null ? null : valueOf(circumferences.hip()),
                    circumferences == null ? null : valueOf(circumferences.thigh()),
                    circumferences == null ? null : valueOf(circumferences.calf()),
                    bodyMetrics.getBmi() == null ? null : bodyMetrics.getBmi().value(),
                    bodyMetrics.getBodyFatPercentage() == null ? null : bodyMetrics.getBodyFatPercentage().value(),
                    bodyMetrics.getFatMassKg() == null ? null : bodyMetrics.getFatMassKg().value(),
                    bodyMetrics.getLeanMassKg() == null ? null : bodyMetrics.getLeanMassKg().value(),
                    bodyMetrics.getUpdatedAt(),
                    bodyMetrics.getNotes()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter domínio para entidade.", exception);
        }
    }

    public static BodyMetrics toDomain(BodyMetricsEntity entity) {
        try {
            return BodyMetrics.restore(
                    new BodyMetricsId(entity.getId()),
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
