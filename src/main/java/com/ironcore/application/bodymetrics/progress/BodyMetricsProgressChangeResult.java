package com.ironcore.application.bodymetrics.progress;

import java.time.LocalDate;

public record BodyMetricsProgressChangeResult(
        BodyMetricsProgressMetric metric,
        String label,
        String unit,
        LocalDate firstDate,
        Double firstValue,
        LocalDate lastDate,
        Double lastValue,
        Double absoluteChange,
        Double percentageChange
) {
}
