package com.ironcore.interfaces.rest.bodymetrics.dto.progress;

import com.ironcore.application.bodymetrics.progress.BodyMetricsProgressMetric;

import java.util.List;

public record BodyMetricsProgressSeriesResponse(
        BodyMetricsProgressMetric metric,
        String label,
        String unit,
        List<BodyMetricsProgressPointResponse> points
) {
}
