package com.ironcore.application.bodymetrics.progress;

import java.time.LocalDate;
import java.util.List;

public record GetBodyMetricsProgressChartResult(
        LocalDate startDate,
        LocalDate endDate,
        BodyMetricsProgressChartType chartType,
        List<BodyMetricsProgressSeriesResult> series
) {
}
