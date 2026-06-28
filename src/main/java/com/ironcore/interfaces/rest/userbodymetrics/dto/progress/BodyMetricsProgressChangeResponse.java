package com.ironcore.interfaces.rest.userbodymetrics.dto.progress;

import com.ironcore.application.userbodymetrics.progress.BodyMetricsProgressMetric;

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
