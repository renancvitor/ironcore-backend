package com.ironcore.interfaces.rest.userbodymetrics.dto.progress;

import com.ironcore.application.userbodymetrics.progress.BodyMetricsProgressMetric;

import java.util.List;

public record BodyMetricsProgressSeriesResponse(
        BodyMetricsProgressMetric metric,
        String label,
        String unit,
        List<BodyMetricsProgressPointResponse> points
) {
}
