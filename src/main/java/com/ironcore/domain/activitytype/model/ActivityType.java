package com.ironcore.domain.activitytype.model;

import com.ironcore.domain.activitytype.exception.InvalidActivityTypeException;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import lombok.Getter;

@Getter
public class ActivityType {

    private final ActivityTypeId id;
    private final ActivityTypeCode code;
    private final String displayName;
    private final Boolean active;
    private final Integer sortOrder;

    public ActivityType(ActivityTypeId id, ActivityTypeCode code, String displayName, Boolean active,
                         Integer sortOrder) {
        this.id = requireNonNull(id, "Id não pode ser nulo.");
        this.code = requireNonNull(code, "Código não pode ser nulo.");
        this.displayName = requireNonBlank(displayName,"Nome de exibição não pode ser nulo ou vazio.");
        this.active = requireNonNull(active, "Status ativo do tipo de atividade não pode ser nulo.");
        this.sortOrder = requireNonNull(sortOrder, "Ordem de exibição do tipo de atividade não pode ser nula.");
    }

    public static ActivityType restore(ActivityTypeId id, ActivityTypeCode code, String displayName, Boolean active,
                                       Integer sortOrder) {
        return new ActivityType(id, code, displayName, active, sortOrder);
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidActivityTypeException(message);
        }

        return value.trim();
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidActivityTypeException(message);
        }

        return value;
    }
}
