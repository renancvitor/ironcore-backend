package com.ironcore.application.userbodymetrics.update;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;

public record UpdateUserBodyMetricsCommand(
        UserBodyMetricsId userBodyMetricsId,
        UserId userId,
        BodyWeightKg weight,
        BodyHeightCm height,
        BodyCircumferences circumferences,
        String notes
) {
}
