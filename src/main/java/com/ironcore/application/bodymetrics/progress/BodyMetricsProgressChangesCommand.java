package com.ironcore.application.bodymetrics.progress;

import com.ironcore.domain.user.valueobject.UserId;

import java.time.LocalDate;

public record BodyMetricsProgressChangesCommand(
        UserId userId,
        LocalDate startDate,
        LocalDate endDate
) {
}
