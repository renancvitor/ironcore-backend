package com.ironcore.application.bodymetrics.progress;

import java.time.LocalDate;
import java.util.List;

public record GetBodyMetricsProgressChangesResult(
        LocalDate startDate,
        LocalDate endDate,
        List<BodyMetricsProgressChangeResult> changes
) {
}
