package com.ironcore.application.bodymetrics.progress;

import com.ironcore.domain.user.valueobject.UserId;

import java.time.LocalDate;

public record BodyMetricsProgressChartCommand(
        UserId userId,
        BodyMetricsProgressChartType chartType,
        LocalDate startDate,
        LocalDate endDate
) {
}
