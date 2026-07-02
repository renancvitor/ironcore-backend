package com.ironcore.interfaces.rest.bodymetrics.dto.progress;

import com.ironcore.application.bodymetrics.progress.BodyMetricsProgressChartType;

import java.time.LocalDate;
import java.util.List;

public record BodyMetricsProgressChartResponse(
        LocalDate startDate,
        LocalDate endDate,
        BodyMetricsProgressChartType chartType,
        List<BodyMetricsProgressSeriesResponse> series
) {
}
