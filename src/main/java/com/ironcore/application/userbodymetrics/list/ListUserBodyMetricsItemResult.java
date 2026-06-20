package com.ironcore.application.userbodymetrics.list;

import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;

import java.time.LocalDateTime;

public record ListUserBodyMetricsItemResult(
        UserBodyMetricsId id,
        LocalDateTime measuredAt,
        BodyWeightKg weightKg,
        BodyHeightCm heightCm,
        String notes
) {
}
