package com.ironcore.infrastructure.persistence.exercise;

import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;

import java.time.LocalDateTime;

import static com.ironcore.infrastructure.persistence.activitytype.ActivityTypeEntityTestFactory.activityTypeEntity;
import static com.ironcore.infrastructure.persistence.equipmenttype.EquipmentTypeEntityTestFactory.equipmentTypeEntity;

public final class ExerciseEntityTestFactory {

    private ExerciseEntityTestFactory() {
    }

    public static ExerciseEntity exerciseEntity() {
        return exerciseEntity(1L);
    }

    public static ExerciseEntity invalidExerciseEntity() {
        return exerciseEntity(null);
    }

    private static ExerciseEntity exerciseEntity(Long id) {
        return new ExerciseEntity(
                id,
                "Supino reto",
                equipmentTypeEntity(),
                activityTypeEntity(),
                false,
                true,
                90,
                true,
                LocalDateTime.of(2026, 7, 12, 10, 0),
                LocalDateTime.of(2026, 7, 12, 11, 0)
        );
    }
}
