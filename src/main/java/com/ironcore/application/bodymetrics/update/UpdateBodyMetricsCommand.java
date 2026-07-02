package com.ironcore.application.bodymetrics.update;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;

public record UpdateBodyMetricsCommand(
        BodyMetricsId bodyMetricsId,
        UserId userId,
        BodyWeightKg weight,
        BodyHeightCm height,
        BodyCircumferences circumferences,
        String notes
) {
}
