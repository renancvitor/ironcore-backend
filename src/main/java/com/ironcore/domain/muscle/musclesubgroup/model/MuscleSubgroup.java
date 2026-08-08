package com.ironcore.domain.muscle.musclesubgroup.model;

import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.exception.InvalidMuscleSubgroupException;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import lombok.Getter;

@Getter
public class MuscleSubgroup {

    private final MuscleSubgroupId id;
    private final MuscleGroupId muscleGroupId;
    private final MuscleSubgroupCode code;
    private final String displayName;
    private final Boolean active;
    private final Integer sortOrder;

    private MuscleSubgroup(MuscleSubgroupId id, MuscleGroupId muscleGroupId, MuscleSubgroupCode code,
                           String displayName, Boolean active, Integer sortOrder) {
        this.id = requireNonNull(id, "Id não pode ser nulo.");
        this.muscleGroupId = requireNonNull(muscleGroupId, "Id do grupo muscular não pode ser nulo.");
        this.code = requireNonNull(code, "Código não pode ser nulo.");
        this.displayName = requireNonBlank(displayName, "Nome de exibição não pode ser nulo ou vazio.");
        this.active = requireNonNull(active, "Status ativo do subgrupo muscular não pode ser nulo.");
        this.sortOrder = requireNonNull(sortOrder, "Ordem de exibição do subgrupo muscular não pode ser nula.");
    }

    public static MuscleSubgroup restore(MuscleSubgroupId id, MuscleGroupId muscleGroupId, MuscleSubgroupCode code,
                                         String displayName, Boolean active, Integer sortOrder) {
        return new MuscleSubgroup(id, muscleGroupId, code, displayName, active, sortOrder);
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidMuscleSubgroupException(message);
        }

        return value.trim();
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidMuscleSubgroupException(message);
        }

        return value;
    }
}
