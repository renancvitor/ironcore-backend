package com.ironcore.application.bodymetrics.latest;

import com.ironcore.domain.user.valueobject.UserId;

public record GetLatestBodyMetricsCommand(
        UserId actorUserId
) {
}
