package com.ironcore.interfaces.rest.userbodymetrics.dto.progress;

import java.time.LocalDate;
import java.util.List;

public record BodyMetricsProgressChangesResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<BodyMetricsProgressChangeResponse> changes
) {
}
