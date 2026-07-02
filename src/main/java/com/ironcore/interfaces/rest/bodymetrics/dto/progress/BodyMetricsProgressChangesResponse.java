package com.ironcore.interfaces.rest.bodymetrics.dto.progress;

import java.time.LocalDate;
import java.util.List;

public record BodyMetricsProgressChangesResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<BodyMetricsProgressChangeResponse> changes
) {
}
