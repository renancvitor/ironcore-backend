package com.ironcore.domain.muscle.musclegroup.model;

import com.ironcore.domain.muscle.musclegroup.exception.InvalidMuscleGroupException;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import lombok.Getter;

@Getter
public class MuscleGroup {

    private final MuscleGroupId id;
    private final MuscleGroupCode code;
    private final String displayName;
    private final Boolean active;
    private final Integer sortOrder;

    public MuscleGroup(MuscleGroupId id, MuscleGroupCode code, String displayName, Boolean active, Integer sortOrder) {
        this.id = requireNonNull(id, "Id não pode ser nulo.");
        this.code = requireNonNull(code, "Código não pode ser nulo.");
        this.displayName = requireNonBlank(displayName, "Nome de exibição não pode ser nulo ou vazio.");
        this.active = requireNonNull(active, "Status ativo do grupo muscular não pode ser nulo.");
        this.sortOrder = requireNonNull(sortOrder, "Ordem de exibição do grupo muscular não pode ser nula.");
    }

    public static MuscleGroup restore(MuscleGroupId id, MuscleGroupCode code, String displayName, Boolean active,
                                      Integer sortOrder) {
        return new MuscleGroup(id, code, displayName, active, sortOrder);
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidMuscleGroupException(message);
        }

        return value.trim();
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidMuscleGroupException(message);
        }

        return value;
    }
}
