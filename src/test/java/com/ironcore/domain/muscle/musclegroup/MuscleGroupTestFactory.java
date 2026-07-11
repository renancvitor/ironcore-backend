package com.ironcore.domain.muscle.musclegroup;

import com.ironcore.domain.muscle.musclegroup.model.MuscleGroup;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;

public final class MuscleGroupTestFactory {

    private MuscleGroupTestFactory() {
    }

    public static MuscleGroup restoreMuscleGroup() {
        return MuscleGroup.restore(
                new MuscleGroupId(1L),
                new MuscleGroupCode(" back "),
                " Costas ",
                true,
                20
        );
    }

    public static MuscleGroupCode code(String value) {
        return new MuscleGroupCode(value);
    }
}
