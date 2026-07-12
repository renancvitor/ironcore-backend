package com.ironcore.domain.muscle.musclesubgroup;

import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.model.MuscleSubgroup;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;

public final class MuscleSubgroupTestFactory {

    private MuscleSubgroupTestFactory() {
    }

    public static MuscleSubgroup restoreMuscleSubgroup() {
        return MuscleSubgroup.restore(
                new MuscleSubgroupId(1L),
                new MuscleGroupId(1L),
                new MuscleSubgroupCode(" deltoid "),
                "Deltoide",
                true,
                10
        );
    }

    public static MuscleSubgroupCode code(String value) {
        return new MuscleSubgroupCode(value);
    }
}
