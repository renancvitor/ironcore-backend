package com.ironcore.domain.activitytype;

import com.ironcore.domain.activitytype.model.ActivityType;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;

public final class ActivityTypeTestFactory {

    private ActivityTypeTestFactory() {
    }

    public static ActivityType restoreActivityType() {
        return ActivityType.restore(
                new ActivityTypeId(1L),
                new ActivityTypeCode(" strength "),
                " Força ",
                true,
                10
        );
    }

    public static ActivityTypeCode code(String value) {
        return new ActivityTypeCode(value);
    }
}
