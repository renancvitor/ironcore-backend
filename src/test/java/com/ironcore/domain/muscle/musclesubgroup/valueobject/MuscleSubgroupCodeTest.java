package com.ironcore.domain.muscle.musclesubgroup.valueobject;

import com.ironcore.domain.muscle.musclesubgroup.exception.InvalidMuscleSubgroupException;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.muscle.musclesubgroup.MuscleSubgroupTestFactory.code;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MuscleSubgroupCodeTest {

    @Test
    void shouldNormalizeCode() {
        MuscleSubgroupCode code = code(" deltoid ");

        assertThat(code.value()).isEqualTo("DELTOID");
    }

    @Test
    void shouldRejectNullCode() {
        assertThatThrownBy(() -> new MuscleSubgroupCode(null))
                .isInstanceOf(InvalidMuscleSubgroupException.class);
    }

    @Test
    void shouldRejectWhenCodeIsBlank() {
        assertThatThrownBy(() -> new MuscleSubgroupCode(" "))
                .isInstanceOf(InvalidMuscleSubgroupException.class);
    }

    @Test
    void shouldRejectInvalidCodeFormat() {
        assertThatThrownBy(() -> new MuscleSubgroupCode("invalid-@"))
                .isInstanceOf(InvalidMuscleSubgroupException.class);
    }

    @Test
    void shouldRejectLengthGreaterThan50() {
        assertThatThrownBy(() -> new MuscleSubgroupCode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
                .isInstanceOf(InvalidMuscleSubgroupException.class);
    }
}
