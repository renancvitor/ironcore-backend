package com.ironcore.application.userbodymetrics.progress;

import java.time.LocalDate;
import java.util.List;

public record GetBodyMetricsProgressChartResult(
        LocalDate startDate,
        LocalDate endDate,
        BodyMetricsProgressChartType type,
        List<BodyMetricsProgressSeriesResult> series
) {
}
