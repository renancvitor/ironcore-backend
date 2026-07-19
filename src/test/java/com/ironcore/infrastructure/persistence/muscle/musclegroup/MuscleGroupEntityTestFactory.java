package com.ironcore.infrastructure.persistence.muscle.musclegroup;

import com.ironcore.infrastructure.persistence.muscle.musclegroup.entity.MuscleGroupEntity;

public final class MuscleGroupEntityTestFactory {

    private MuscleGroupEntityTestFactory() {
    }

    public static MuscleGroupEntity muscleGroupEntity() {
        return muscleGroupEntity(1L);
    }

    public static MuscleGroupEntity invalidMuscleGroupEntity() {
        return muscleGroupEntity(null);
    }

    private static MuscleGroupEntity muscleGroupEntity(Long id) {
        return new MuscleGroupEntity(
                id,
                "BACK",
                "Costas",
                true,
                20
        );
    }
}
