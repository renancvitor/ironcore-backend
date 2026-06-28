package com.ironcore.interfaces.rest.userbodymetrics.dto.progress;

import com.ironcore.application.userbodymetrics.progress.BodyMetricsProgressChartType;

import java.time.LocalDate;
import java.util.List;

public record BodyMetricsProgressChartResponse(
        LocalDate startDate,
        LocalDate endDate,
        BodyMetricsProgressChartType chartType,
        List<BodyMetricsProgressSeriesResponse> series
) {
}
