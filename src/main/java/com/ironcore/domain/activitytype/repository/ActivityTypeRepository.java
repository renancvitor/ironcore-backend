package com.ironcore.domain.activitytype.repository;

import com.ironcore.domain.activitytype.model.ActivityType;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;

import java.util.Optional;

public interface ActivityTypeRepository {

    Optional<ActivityType> findById(ActivityTypeId id);

    Optional<ActivityType> findByCode(ActivityTypeCode code);
}
