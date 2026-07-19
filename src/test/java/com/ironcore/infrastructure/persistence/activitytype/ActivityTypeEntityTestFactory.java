package com.ironcore.infrastructure.persistence.activitytype;

import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;

public final class ActivityTypeEntityTestFactory {

    private ActivityTypeEntityTestFactory() {
    }

    public static ActivityTypeEntity activityTypeEntity() {
        return activityTypeEntity(1L);
    }

    public static ActivityTypeEntity invalidActivityTypeEntity() {
        return activityTypeEntity(null);
    }

    private static ActivityTypeEntity activityTypeEntity(Long id) {
        return new ActivityTypeEntity(
                id,
                "STRENGTH",
                "Força",
                true,
                10
        );
    }
}
