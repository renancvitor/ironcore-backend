package com.ironcore.domain.exercise;

import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import java.time.LocalDateTime;

public final class ExerciseTestFactory {

    private ExerciseTestFactory() {
    }

    public static Exercise restoreExercise() {
        return Exercise.restore(
                new ExerciseId(1L),
                " Supino reto ",
                new EquipmentTypeId(1L),
                new ActivityTypeId(1L),
                false,
                true,
                90,
                true,
                LocalDateTime.of(2026, 7, 12, 10, 0),
                LocalDateTime.of(2026, 7, 12, 11, 0));
    }

    public static Exercise restoreInactiveExercise() {
        return Exercise.restore(
                new ExerciseId(1L),
                " Supino reto ",
                new EquipmentTypeId(1L),
                new ActivityTypeId(1L),
                false,
                true,
                90,
                false,
                LocalDateTime.of(2026, 7, 12, 10, 0),
                LocalDateTime.of(2026, 7, 12, 11, 0));
    }
}
