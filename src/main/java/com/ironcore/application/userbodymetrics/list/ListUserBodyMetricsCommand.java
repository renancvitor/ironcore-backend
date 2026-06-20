package com.ironcore.application.userbodymetrics.list;

import com.ironcore.domain.user.valueobject.UserId;

public record ListUserBodyMetricsCommand(
        UserId userId,
        int page,
        int size
) {
}
