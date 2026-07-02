package com.ironcore.application.bodymetrics.list;

import com.ironcore.domain.user.valueobject.UserId;

public record ListBodyMetricsCommand(
        UserId userId,
        int page,
        int size
) {
}
