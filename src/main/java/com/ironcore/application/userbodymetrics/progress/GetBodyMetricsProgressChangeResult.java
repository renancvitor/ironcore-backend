package com.ironcore.application.userbodymetrics.progress;

import java.time.LocalDate;
import java.util.List;

public record GetBodyMetricsProgressChangeResult(
        LocalDate startDate,
        LocalDate endDate,
        List<BodyMetricsProgressChangeResult> changes
) {
}
