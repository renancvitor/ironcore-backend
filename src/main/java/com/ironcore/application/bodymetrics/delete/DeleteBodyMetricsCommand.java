package com.ironcore.application.bodymetrics.delete;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;

public record DeleteBodyMetricsCommand(
        BodyMetricsId bodyMetricsId,
        UserId userId
) {
}
