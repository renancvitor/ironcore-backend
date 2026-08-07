package com.ironcore.domain.workoutplanning.traininggoal.model;

import com.ironcore.domain.workoutplanning.traininggoal.exception.InvalidTrainingGoalException;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalCode;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import lombok.Getter;

@Getter
public class TrainingGoal {

    private final TrainingGoalId id;
    private final TrainingGoalCode code;
    private final String displayName;
    private final Boolean active;
    private final Integer sortOrder;

    public TrainingGoal(TrainingGoalId id, TrainingGoalCode code, String displayName, Boolean active,
                        Integer sortOrder) {
        this.id = requireNonNull(id, "Id não pode ser nulo.");
        this.code = requireNonNull(code, "Código não pode ser nulo.");
        this.displayName = requireNonBlank(displayName, "Nome de exibição não pode ser nulo ou vazio.");
        this.active = requireNonNull(active, "Status ativo do objetivo de treino não pode ser nulo.");
        this.sortOrder = requireNonNull(sortOrder, "Ordem de exibição do objetivo de treino não pode ser nula.");
    }

    public static TrainingGoal restore(TrainingGoalId id, TrainingGoalCode code, String displayName, Boolean active,
                                       Integer sortOrder) {
        return new TrainingGoal(id, code, displayName, active, sortOrder);
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidTrainingGoalException(message);
        }

        return value.trim();
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidTrainingGoalException(message);
        }

        return value;
    }
}
