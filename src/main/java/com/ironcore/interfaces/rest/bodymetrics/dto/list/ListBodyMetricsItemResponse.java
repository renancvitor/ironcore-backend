package com.ironcore.interfaces.rest.bodymetrics.dto.list;

import java.time.LocalDateTime;

public record ListBodyMetricsItemResponse(
        Long id,
        LocalDateTime measuredAt,
        Double weightKg,
        Double heightCm,
        String notes
) {
}
