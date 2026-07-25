package com.ironcore.application.exercise.catalog.result;

import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;

public record ActivityTypeItemResult(
        ActivityTypeId id,
        ActivityTypeCode code,
        String name
) {
}
