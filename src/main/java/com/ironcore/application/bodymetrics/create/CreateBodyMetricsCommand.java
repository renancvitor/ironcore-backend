package com.ironcore.application.bodymetrics.create;

import com.ironcore.domain.bodymetrics.valueobject.BodyCircumferences;
import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.user.valueobject.UserId;

public record CreateBodyMetricsCommand(
        UserId userId,
        BodyWeightKg weight,
        BodyHeightCm height,
        BodyCircumferences circumferences,
        String notes
) {
}
