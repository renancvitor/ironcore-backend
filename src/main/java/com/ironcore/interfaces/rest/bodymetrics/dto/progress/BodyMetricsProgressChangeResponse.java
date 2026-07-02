package com.ironcore.interfaces.rest.bodymetrics.dto.progress;

import com.ironcore.application.bodymetrics.progress.BodyMetricsProgressMetric;

import java.time.LocalDate;

public record BodyMetricsProgressChangeResponse(
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
