package com.ironcore.infrastructure.persistence.muscle.musclesubgroup;

import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.entity.MuscleSubgroupEntity;

import static com.ironcore.infrastructure.persistence.muscle.musclegroup.MuscleGroupEntityTestFactory.muscleGroupEntity;

public final class MuscleSubgroupEntityTestFactory {

    private MuscleSubgroupEntityTestFactory() {
    }

    public static MuscleSubgroupEntity muscleSubgroupEntity() {
        return muscleSubgroupEntity(1L);
    }

    public static MuscleSubgroupEntity secondaryMuscleSubgroupEntity() {
        return new MuscleSubgroupEntity(
                2L,
                muscleGroupEntity(),
                "LATISSIMUS_DORSI",
                "Latíssimo do dorso",
                true,
                30
        );
    }

    public static MuscleSubgroupEntity invalidMuscleSubgroupEntity() {
        return muscleSubgroupEntity(null);
    }

    private static MuscleSubgroupEntity muscleSubgroupEntity(Long id) {
        return new MuscleSubgroupEntity(
                id,
                muscleGroupEntity(),
                "DELTOID",
                "Deltoide",
                true,
                10
        );
    }
}
