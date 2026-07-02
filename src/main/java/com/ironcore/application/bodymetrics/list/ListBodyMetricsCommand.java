package com.ironcore.application.bodymetrics.list;

import com.ironcore.domain.user.valueobject.UserId;

public record ListBodyMetricsCommand(
        UserId actorUserId,
        int page,
        int size
) {
}
