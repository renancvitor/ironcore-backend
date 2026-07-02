package com.ironcore.application.bodymetrics;

import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.valueobject.BodyCircumferenceCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyCircumferences;

import java.time.LocalDateTime;

public record BodyMetricsAuditData(
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

    public static BodyMetricsAuditData from(BodyMetrics bodyMetrics) {
        BodyCircumferences circumferences = bodyMetrics.getCircumferences();

        return new BodyMetricsAuditData(
                bodyMetrics.getId().value(),
                bodyMetrics.getUserId().value(),
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
                bodyMetrics.getNotes()
        );
    }

    private static Double valueOf(BodyCircumferenceCm circumferenceCm) {
        return circumferenceCm == null ? null : circumferenceCm.value();
    }
}
