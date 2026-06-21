package com.ironcore.application.userbodymetrics.get;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;

public record GetUserBodyMetricsCommand(
        UserBodyMetricsId userBodyMetricsId,
        UserId userId
) {
}
