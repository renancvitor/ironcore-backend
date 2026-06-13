package com.ironcore.application.userbodymetrics;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.valueobject.*;

public record CreateUserBodyMetricsCommand(
        UserId userId,
        BodyWeightKg weight,
        BodyHeightCm height,
        BodyCircumferences circumferences,
        String notes
) {
}
