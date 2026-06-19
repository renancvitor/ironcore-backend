package com.ironcore.application.userbodymetrics.delete;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;

public record DeleteUserBodyMetricsCommand(
        UserBodyMetricsId userBodyMetricsId,
        UserId userId
) {
}
