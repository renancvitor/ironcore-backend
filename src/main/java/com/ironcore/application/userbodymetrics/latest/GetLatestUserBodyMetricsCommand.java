package com.ironcore.application.userbodymetrics.latest;

import com.ironcore.domain.user.valueobject.UserId;

public record GetLatestUserBodyMetricsCommand(
        UserId userId
) {
}
