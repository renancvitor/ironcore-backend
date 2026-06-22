package com.ironcore.interfaces.rest.userbodymetrics.dto.list;

import java.time.LocalDateTime;

public record ListUserBodyMetricsItemResponse(
        Long id,
        LocalDateTime measuredAt,
        Double weightKg,
        Double heightCm,
        String notes
) {
}
