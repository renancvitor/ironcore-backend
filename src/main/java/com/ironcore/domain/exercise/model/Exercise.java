package com.ironcore.domain.exercise.model;

import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.exception.InvalidExerciseException;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Exercise {

    private final ExerciseId id;
    private final String name;
    private final EquipmentTypeId equipmentTypeId;
    private final ActivityTypeId activityTypeId;
    private final Boolean unilateral;
    private final Boolean compound;
    private final Integer suggestedRestSeconds;
    private final Boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Exercise(ExerciseId id, String name, EquipmentTypeId equipmentTypeId, ActivityTypeId activityTypeId,
                    Boolean unilateral, Boolean compound, Integer suggestedRestSeconds, Boolean active,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = requireNonNull(id, "Id não pode ser nulo.");
        this.name = requireNonBlank(name, "Nome não pode ser nulo ou vazio.");
        this.equipmentTypeId = requireNonNull(equipmentTypeId, "Id do tipo de equipamento não pode ser nulo.");
        this.activityTypeId = requireNonNull(activityTypeId, "Id do tipo de atividade não pode ser nulo.");
        this.unilateral = requireNonNull(unilateral, "Tag de exercício unilateral não pode ser nulo.");
        this.compound = requireNonNull(compound, "Tag de exercício composto não pode ser nulo.");
        this.suggestedRestSeconds = requirePositiveIfPresent(suggestedRestSeconds,
                "Descanso sugerido deve ser positivo.");
        this.active = requireNonNull(active, "Tag de exercício ativo não pode ser nulo.");
        this.createdAt = requireNonNull(createdAt, "Data de criação não pode ser nulo.");
        this.updatedAt = updatedAt;
    }

    public static Exercise restore(ExerciseId id, String name, EquipmentTypeId equipmentTypeId,
                                   ActivityTypeId activityTypeId, Boolean unilateral, Boolean compound,
                                   Integer suggestedRestSeconds, Boolean active, LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        return new Exercise(id, name, equipmentTypeId, activityTypeId, unilateral, compound, suggestedRestSeconds,
                active, createdAt, updatedAt);
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidExerciseException(message);
        }

        return value.trim();
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidExerciseException(message);
        }

        return value;
    }

    private Integer requirePositiveIfPresent(Integer value, String message) {
        if (value != null && value <= 0) {
            throw new InvalidExerciseException(message);
        }

        return value;
    }
}
