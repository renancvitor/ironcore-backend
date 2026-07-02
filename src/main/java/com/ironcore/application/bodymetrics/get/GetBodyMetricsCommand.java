package com.ironcore.application.bodymetrics.get;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;

public record GetBodyMetricsCommand(
        BodyMetricsId bodyMetricsId,
        UserId userId
) {
}
