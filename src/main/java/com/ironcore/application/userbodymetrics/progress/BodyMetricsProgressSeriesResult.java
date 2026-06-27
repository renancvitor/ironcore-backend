package com.ironcore.application.userbodymetrics.progress;

import java.util.List;

public record BodyMetricsProgressSeriesResult(
        BodyMetricsProgressMetric metric,
        String label,
        String unit,
        List<BodyMetricsProgressPointResult> points
) {
}
