package com.ironcore.application.userbodymetrics;

import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferenceCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferences;

import java.time.LocalDateTime;

public record UserBodyMetricsAuditData(
        Long id,
        Long userId,
        LocalDateTime measuredAt,
        Double weightKg,
        Double heightCm,
        Double neckCm,
        Double chestCm,
        Double shoulderCm,
        Double armCm,
        Double forearmCm,
        Double waistCm,
        Double hipCm,
        Double thighCm,
        Double calfCm,
        Double bmi,
        Double bodyFatPercentage,
        Double fatMassKg,
        Double leanMassKg,
        String notes
) implements LoggableData {

    public static UserBodyMetricsAuditData from(UserBodyMetrics userBodyMetrics) {
        BodyCircumferences circumferences = userBodyMetrics.getCircumferences();

        return new UserBodyMetricsAuditData(
                userBodyMetrics.getId().value(),
                userBodyMetrics.getUserId().value(),
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
                userBodyMetrics.getNotes()
        );
    }

    private static Double valueOf(BodyCircumferenceCm circumferenceCm) {
        return circumferenceCm == null ? null : circumferenceCm.value();
    }
}
