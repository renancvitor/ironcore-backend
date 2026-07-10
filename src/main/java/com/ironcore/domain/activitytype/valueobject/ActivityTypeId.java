package com.ironcore.domain.activitytype.valueobject;

import com.ironcore.domain.activitytype.exception.InvalidActivityTypeException;

public record ActivityTypeId(Long value) {

    public ActivityTypeId {
        if (value == null || value <= 0) {
            throw new InvalidActivityTypeException("Id do tipo de atividade deve ser positivo.");
        }
    }
}
