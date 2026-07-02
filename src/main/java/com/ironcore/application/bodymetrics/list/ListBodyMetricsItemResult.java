package com.ironcore.application.bodymetrics.list;

import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;

import java.time.LocalDateTime;

public record ListBodyMetricsItemResult(
        BodyMetricsId id,
        LocalDateTime measuredAt,
        BodyWeightKg weightKg,
        BodyHeightCm heightCm,
        String notes
) {
}
